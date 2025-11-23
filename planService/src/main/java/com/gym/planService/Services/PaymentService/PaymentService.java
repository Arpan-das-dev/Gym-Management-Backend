package com.gym.planService.Services.PaymentService;

import com.gym.planService.Dtos.OrderDtos.Requests.ConfirmPaymentDto;
import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;
import com.gym.planService.Dtos.OrderDtos.Responses.RecentTransactionsResponseDto;
import com.gym.planService.Dtos.OrderDtos.Wrappers.AllRecentTransactionsResponseWrapperDto;
import com.gym.planService.Dtos.PlanDtos.Responses.GenericResponse;
import com.gym.planService.Exception.Custom.*;
import com.gym.planService.Models.Plan;
import com.gym.planService.Models.PlanCuponCode;
import com.gym.planService.Models.PlanPayment;
import com.gym.planService.Repositories.PlanCuponCodeRepository;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.PlanRepository;
import com.gym.planService.Services.OtherServices.AwsService;
import com.gym.planService.Services.OtherServices.RazorPayService;
import com.gym.planService.Services.OtherServices.ReceiptGenerator;
import com.gym.planService.Services.OtherServices.WebClientService;
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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Transactional
    @CacheEvict(value = "recentTransactions", allEntries = true )
    public String createOrder(PlanPaymentRequestDto requestDto) {
        log.info("request received for payment of Rs. {}", requestDto.getAmount());
        log.info("cupon code is {}", requestDto.getCuponCode());

        Plan plan = planRepository.findById(requestDto.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("No plan found with ID: " + requestDto.getPlanId()));

        PlanCuponCode cuponCode = cuponCodeRepository.findById(requestDto.getCuponCode()).orElse(null);

        String paymentId = paymentIdGenUtil.generatePaymentId(
                requestDto.getUserId(),
                requestDto.getPlanId(),
                requestDto.getPaymentDate()
        );

        Double finalAmount = calculateDiscountedAmount(cuponCode, requestDto.getAmount());

        Order razorOrder;
        try {
            log.info("final amount to be paid ====> {}", finalAmount);
            razorOrder = razorPayService.makePayment(finalAmount.longValue(), requestDto.getCurrency(), paymentId);
            if (cuponCode != null) {
                cuponCode.setCuponCodeUser(cuponCode.getCuponCodeUser()+1);
                cuponCodeRepository.save(cuponCode);
            }
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
            @CacheEvict(value = "recentTransactions", allEntries = true )
    })
    public Mono<GenericResponse> confirmPayment(ConfirmPaymentDto dto) {
        long startTime = System.currentTimeMillis();
        log.info("Confirming payment for order ID: {}", dto.getOrderId());

        PlanPayment payment = paymentRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> {
                    log.error("Payment not found for order ID: {}", dto.getOrderId());
                    return new PaymentNotFoundException("No payment found for this order ID");
                });

        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionTime(LocalDateTime.now());

        Plan plan = planRepository.findById(payment.getPlanId())
                .orElseThrow(() -> {
                    log.error("Plan not found for ID: {}", payment.getPlanId());
                    return new PlanNotFoundException("No plan found with ID: " + payment.getPlanId());
                });


        // A. Receipt Generation and S3 Upload Flow (PDF generation is now reactive/async)
        Mono<Tuple2<String, byte[]>> receiptFlowMono = Mono
                .fromFuture(receiptGenerator.generatePlanPaymentReceipt(payment))
                .doOnSuccess(success -> log.debug("PDF bytes generated successfully of size {}",success.length))
                .flatMap(pdfBytes -> Mono.fromFuture(awsService.uploadPaymentReceipt(pdfBytes, payment.getPaymentId()))
                        // Pass both the URL and the original PDF bytes needed for the email
                        .map(receiptUrl -> Tuples.of(receiptUrl, pdfBytes)));

        // B. Member Service Update Flow
        Mono<String> memberServiceResponseMono = webClientService
                .askMemberServiceToAppendPlan(plan, payment.getUserId())
                .doOnSubscribe(s -> log.info("Initiating member service call"))
                .doOnNext(res -> log.info("Received response from member service: {}", res));


        // --- 3. Combine and Process (Non-Blocking) ---
        return Mono.zip(receiptFlowMono, memberServiceResponseMono)
                .flatMap(tuple -> {
                    String receiptUrl = tuple.getT1().getT1();
                    byte[] pdfReceiptArray = tuple.getT1().getT2();
                    String message = tuple.getT2();

                    log.info("Received final response from member service and receipt URL: {}", receiptUrl);
                    log.info("Total processing time until response: {} ms", System.currentTimeMillis() - startTime);

                    // C. Update synchronous entities
                    plan.setMembersCount(plan.getMembersCount() + 1);
                    planRepository.save(plan);

                    payment.setReceiptUrl(receiptUrl);
                    paymentRepository.save(payment);

                    // D. Fire-and-Forget Email
                    try {
                        // Assuming sendUpdateBymMailWithAttachment returns a CompletableFuture<Void> or similar
                        // Use .subscribe() to execute it asynchronously without blocking the main flow.
                        webClientService
                                .sendUpdateBymMailWithAttachment(pdfReceiptArray, payment, plan.getDuration(), dto.getUserMail());

                        log.info("Email service call initiated asynchronously for user: {}", dto.getUserMail());
                    } catch (Exception e) {
                        // Catch sync exception from method call, but don't re-throw as fatal to transaction
                        log.warn("Synchronous error during email initiation for {}: {}", dto.getUserMail(), e.getMessage());
                    }

                    return Mono.just(new GenericResponse(message));
                })
                .onErrorMap(e -> {
                    log.error("Transaction failed during reactive flow: {}", e.getMessage());
                    String res = "Payment Done but failed to complete post-payment processing. Contact support for receipt.";
                    // If the transaction fails here, @Transactional will roll back DB changes.
                    return new InterServiceCommunicationException(res);
                });
    }

    private Double calculateDiscountedAmount(PlanCuponCode cuponCode, Double amount) {
        if (cuponCode == null) return amount;
        double discount = (cuponCode.getPercentage() / 100) * amount;
        return amount - discount;
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



}
