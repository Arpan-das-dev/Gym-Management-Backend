package com.gym.planService.Services.PaymentService;

import com.gym.planService.Dtos.CuponDtos.Responses.CuponValidationResponseDto;
import com.gym.planService.Dtos.OrderDtos.Requests.ConfirmPaymentDto;
import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;
import com.gym.planService.Dtos.OrderDtos.Responses.ReceiptResponseDto;
import com.gym.planService.Dtos.OrderDtos.Responses.RecentTransactionsResponseDto;
import com.gym.planService.Dtos.OrderDtos.Wrappers.AllRecentTransactionsResponseWrapperDto;
import com.gym.planService.Dtos.OrderDtos.Wrappers.ReceiptResponseWrapperDto;
import com.gym.planService.Dtos.PlanDtos.Responses.GenericResponse;
import com.gym.planService.Exception.Custom.*;
import com.gym.planService.Models.Plan;
import com.gym.planService.Models.PlanPayment;
import com.gym.planService.Repositories.PlanCuponCodeRepository;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.PlanRepository;
import com.gym.planService.Services.OtherServices.AwsService;
import com.gym.planService.Services.OtherServices.RazorPayService;
import com.gym.planService.Services.OtherServices.ReceiptGenerator;
import com.gym.planService.Services.OtherServices.WebClientService;
import com.gym.planService.Services.PlanServices.CuponCodeManagementService;
import com.gym.planService.Utils.PaymentIdGenUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorPayService razorPayService;
    private final PlanRepository planRepository;
    private final PlanCuponCodeRepository cuponCodeRepository;
    private final PlanPaymentRepository paymentRepository;
    private final PaymentIdGenUtil paymentIdGenUtil;
    private final ReceiptGenerator receiptGenerator;
    private final AwsService awsService;
    private final WebClientService webClientService;
    private final StringRedisTemplate redisTemplate;
    private final CuponCodeManagementService cuponCodeManagementService;

    @Transactional
    @CacheEvict(value = "recentTransactions", allEntries = true )
    public String createOrder(PlanPaymentRequestDto requestDto) {
        log.info("request received for payment of Rs. {}", requestDto.getAmount());
        log.info("cupon code is {}", requestDto.getCuponCode());

        Plan plan = planRepository.findById(requestDto.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("No plan found with ID: " + requestDto.getPlanId()));

        CuponValidationResponseDto validationResponse = null;
        if(requestDto.getCuponCode()!= null) {
          validationResponse  = cuponCodeManagementService
                    .validateCupon(requestDto.getCuponCode(),requestDto.getPlanId());
        }

        if(validationResponse!=null && !validationResponse.isValid()){
            throw new CuponCodeNotFoundException("No Coupon Associated With "+plan.getPlanName()+"Plan");
        }
        String paymentId = paymentIdGenUtil.generatePaymentId(
                requestDto.getUserId(),
                requestDto.getPlanId(),
                requestDto.getPaymentDate()
        );

        Double finalAmount = calculateDiscountedAmount(requestDto.getAmount(),validationResponse);

        Order razorOrder;
        try {
            log.info("final amount to be paid ====> {}", finalAmount);
            razorOrder = razorPayService.makePayment(finalAmount.longValue(), requestDto.getCurrency(), paymentId);
            int effectedRows = cuponCodeRepository.incrementCuponUsageCount(requestDto.getCuponCode());
            log.info("effected rows for updating cupon code {} usage counts",effectedRows);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for user {}: {}", requestDto.getUserId(), e.getMessage());
            throw new PaymentGatewayException("Unable to initiate payment. Please try again later.");
        }

        PlanPayment payment = PlanPayment.builder()
                .paymentId(paymentId)
                .userName(requestDto.getUserName())
                .userId(requestDto.getUserId())
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .paidPrice(finalAmount)
                .currency(requestDto.getCurrency())
                .paymentMethod("RAZORPAY")
                .paymentStatus("PENDING")
                .orderId(razorOrder.get("id"))
                .paymentDate(requestDto.getPaymentDate().toLocalDate())
                .paymentMonth(requestDto.getPaymentDate().getMonth().toString())
                .paymentYear(requestDto.getPaymentDate().getYear())
                .transactionTime(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        log.info("Order created for user: {} | Plan: {} | OrderID: {}",
                requestDto.getUserId(), plan.getPlanName(), razorOrder.get("id"));

        // ✅ Return only the Razorpay orderId
        return razorOrder.get("id");
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "totalUsers", key = "'totalUsersList'"),
            @CacheEvict(value = "recentTransactions", allEntries = true ),
            @CacheEvict(value = "allRevenue", allEntries = true),
            @CacheEvict(value = "monthlyRevenue", key = "'revenue'"),
            @CacheEvict(value = "mostPopular", key = "'popular'")
    })
    public Mono<GenericResponse> confirmPayment(ConfirmPaymentDto dto) {
        long startTime = System.currentTimeMillis();
        log.info("Confirming payment for order ID: {}", dto.getOrderId());

        PlanPayment payment = paymentRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> {
                    log.error("Payment not found for order ID: {}", dto.getOrderId());
                    return new PaymentNotFoundException("No payment found for this order ID");
                });

        Plan plan = planRepository.findById(payment.getPlanId())
                .orElseThrow(() -> {
                    log.error("Plan not found for ID: {}", payment.getPlanId());
                    return new PlanNotFoundException("No plan found with ID: " + payment.getPlanId());
                });
        Mono<Tuple2<String, byte[]>> receiptFlowMono = generateAndUploadReceipt(payment);
        Mono<String> memberServiceResponseMono = webClientService
                .askMemberServiceToAppendPlan(plan, payment.getUserId())
                .doOnSubscribe(s -> log.info("Initiating member service call"))
                .doOnNext(res -> log.info("Received response from member service: {}", res))
                .onErrorResume(err-> handlePaymentFailure(payment,err,dto.getUserMail()));

        // --- 3. Combine and Process  ---
        return Mono.zip(receiptFlowMono, memberServiceResponseMono)
                .flatMap(tuple -> {
                    String receiptUrl = tuple.getT1().getT1();
                    byte[] pdfReceiptArray = tuple.getT1().getT2();
                    String message = tuple.getT2();

                    log.info("Received final response from member service and receipt URL:[ {} ]", receiptUrl);
                    return Mono.fromCallable(() -> {
                                payment.setPaymentStatus("SUCCESS");
                                payment.setTransactionTime(LocalDateTime.now());
                                payment.setReceiptUrl(receiptUrl);
                                paymentRepository.save(payment);
                                planRepository.incrementMembersCount(plan.getPlanId());
                                evictUserReceiptCache(payment.getUserId());
                                redisTemplate.delete("USERS::" + plan.getPlanId());
                                return payment;
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(savedPayment -> {
                                try {
                                    webClientService
                                            .sendUpdateBymMailWithAttachment(pdfReceiptArray, savedPayment,
                                                    plan.getDuration(), dto.getUserMail());
                                } catch (Exception e) {
                                    log.error("Email failed for user {}, but payment is successful: {}",
                                            dto.getUserMail(), e.getMessage());
                                }
                                log.info("Total processing time: {} ms", System.currentTimeMillis() - startTime);
                                return Mono.just(new GenericResponse(message));
                            });
                });
    }

    private Mono<Tuple2<String, byte[]>> generateAndUploadReceipt(PlanPayment payment){
        return Mono
                .fromFuture(receiptGenerator.generatePlanPaymentReceipt(payment))
                .doOnSuccess(success -> log.debug("PDF bytes generated successfully of size {}",success.length))
                .flatMap(pdfBytes -> Mono
                        .fromFuture(awsService.uploadPaymentReceipt(pdfBytes, payment.getPaymentId()))
                        // Pass both the URL and the original PDF bytes needed for the email
                        .map(receiptUrl -> Tuples.of(receiptUrl, pdfBytes)));
    }
    private Double calculateDiscountedAmount(Double amount,CuponValidationResponseDto responseDto) {
        if(responseDto== null) return amount;
        double discount = (responseDto.getOffPercentage() / 100) * amount;
        return amount - discount;
    }

    private Mono<String> handlePaymentFailure(PlanPayment payment, Throwable err,String userMail) {
        log.info("💀💀 an error occurred during memberservice's plan appending due to {}",err.getMessage());
        return Mono.<String>fromCallable(() -> {
            try {
                String refundId = razorPayService.refundPayment(payment.getPaymentId(), payment.getPaidPrice(), "Service Failure");
                payment.setPaymentStatus("REFUNDED");
                payment.setTransactionTime(LocalDateTime.now());
                paymentRepository.save(payment);
                webClientService.informUserForFailedCase("FAILED", payment,userMail);
                throw new PaymentFailedException(
                        "Plan activation failed, but don't worry! We've initiated a full refund. "
                                + refundId
                                + " No action required. Refund should reflect in 5-7 days."
                );
            } catch (Exception e) {
                payment.setPaymentStatus("MANUAL_INTERVENTION");
                payment.setTransactionTime(LocalDateTime.now());
                paymentRepository.save(payment);
                webClientService.informUserForFailedCase("CRITICAL", payment,userMail);
                throw new RefundFailedException(
                        "💀💀 Plan activation failed and refund encountered an issue. "
                                + payment.getPaymentId()+
                                " Please contact support immediately with your Payment ID."
                );
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    @Cacheable(
            value = "recentTransactions",
            key = "'search=' + (#searchBy ?: '') + ':sort=' + #sortBy + ':dir=' + #sortDirection + ':p=' + #pageNo + ':s=' + #pageSize"
    )
    public AllRecentTransactionsResponseWrapperDto getPaginatedRecentTransactionWithSort(
            String searchBy, String sortBy, String sortDirection, int pageNo, int pageSize
    ) {

        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("➡ [SERVICE CALL] getPaginatedRecentTransactionWithSort() @ " + now);

        log.info("Admin Fetch Request | search='{}' | sort='{}' | dir='{}' | page={} | size={}",
                searchBy, sortBy, sortDirection, pageNo, pageSize);

        String search = (searchBy == null || searchBy.isBlank()) ? "" : searchBy.trim();

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection.trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = switch (sortBy.toLowerCase()) {
            case "amount" -> Sort.by(direction, "paidPrice");
            case "name" -> Sort.by(direction, "userName");
            case "date" -> Sort.by(direction, "transactionTime");
            default -> {
                log.warn("Unknown sortBy='{}', defaulting to transactionTime DESC", sortBy);
                yield Sort.by(Sort.Direction.DESC, "transactionTime");
            }
        };

        Pageable page = PageRequest.of(pageNo, pageSize, sort);

        long start = System.currentTimeMillis();

        Page<PlanPayment> payments = search.isEmpty()
                ? paymentRepository.findAll(page)
                : paymentRepository.searchByUserNameOrDate(search, page);

        long end = System.currentTimeMillis();
        log.info("DB Query executed in {} ms | count={}", (end - start), payments.getTotalElements());

        List<RecentTransactionsResponseDto> transactions = payments.getContent().stream()
                .map(t -> RecentTransactionsResponseDto.builder()
                        .paymentId(t.getPaymentId())
                        .orderId(t.getOrderId())
                        .userName(t.getUserName())
                        .userId(t.getUserId())
                        .planName(t.getPlanName())
                        .planId(t.getPlanId())
                        .paidPrice(t.getPaidPrice())
                        .paymentStatus(t.getPaymentStatus())
                        .paymentMethod(t.getPaymentMethod())
                        .paymentDate(t.getPaymentDate())
                        .paymentTime(t.getTransactionTime())
                        .receiptUrl(t.getReceiptUrl())
                        .build())
                .toList();

        log.info("Mapping complete | {} records processed", transactions.size());

        return AllRecentTransactionsResponseWrapperDto.builder()
                .responseDtoList(transactions)
                .pageNo(payments.getNumber())
                .pageSize(payments.getSize())
                .totalElements(payments.getTotalElements())
                .totalPages(payments.getTotalPages())
                .lastPage(payments.isLast())
                .build();
    }


    @Cacheable(
            value = "receiptCache",
            key = "#userId + ':' + #status + ':' + #sortBy + ':' + #sortDirection + ':' + #pageNo + ':' + #pageSize"
    )
    public ReceiptResponseWrapperDto getReceiptForUsers(String userId, String sortDirection, String sortBy,
                                                        String status, int pageNo, int pageSize) {
        log.info("®️®️ Request received to get receipt for user {}", userId);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort;
        switch (sortBy.trim().toUpperCase()) {
            case "YEAR" -> sort = Sort.by(direction, "paymentYear");
            case "TRANSACTION" -> sort = Sort.by(direction, "transactionTime");
            default -> sort = Sort.by(direction, "paymentDate");
        }
        if(!status.equalsIgnoreCase("ALL")){
            status = status.equalsIgnoreCase("PENDING") ? "PENDING" : "SUCCESS";
        }
        Pageable pageable = PageRequest.of(pageNo,pageSize,sort);
        Page<PlanPayment> payments = paymentRepository.findReceiptCustomUsers(userId,status,pageable);

        return ReceiptResponseWrapperDto.builder()
                .responseDtoList(payments.stream().map(payment -> ReceiptResponseDto.builder()
                        .planName(payment.getPlanName())
                        .paidPrice(BigDecimal.valueOf(payment.getPaidPrice())
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue())
                        .paymentDate(payment.getPaymentDate())
                        .status(payment.getPaymentMethod().toUpperCase())
                        .receiptUrl(payment.getReceiptUrl())
                        .build()).toList())
                .pageNo(payments.getNumber())
                .pageSize(payments.getSize())
                .totalElements(payments.getTotalElements())
                .totalPages(payments.getTotalPages())
                .lastPage(payments.isLast())
                .build();
    }

    /**
     * Evicts all paginated receipt/transaction cache entries for a specific user.
     * Pattern: receiptCache::[userId]*
     * @param userId to evict cache for users
     */
    public void evictUserReceiptCache(String userId) {
        final String cacheName = "receiptCache";
        String pattern = cacheName + "::" + userId + "*";

        Set<String> keysToDelete = safeKeys(pattern);

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("Successfully evicted {} receipt cache keys for userId: {}",
                    keysToDelete.size(), userId);
        } else {
            log.debug("No receipt cache keys found for userId: {}", userId);
        }
    }
    private Set<String> safeKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null) return Collections.emptySet();
        return keys;
    }
}
