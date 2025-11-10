package com.gym.planService.Services.PaymentService;

import com.gym.planService.Dtos.OrderDtos.Requests.ConfirmPaymentDto;
import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;
import com.gym.planService.Exception.Custom.EmailSendFailedException;
import com.gym.planService.Exception.Custom.PaymentGatewayException;
import com.gym.planService.Exception.Custom.PaymentNotFoundException;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

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
    public String confirmPayment(ConfirmPaymentDto dto)  {
        log.info("Confirming payment for order ID: {}", dto.getOrderId());

        PlanPayment payment = paymentRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for this order ID"));

        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionTime(LocalDateTime.now());

        Plan plan = planRepository.findById(payment.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("No plan found with ID: " + payment.getPlanId()));

        // ✅ Generate PDF only after payment confirmation
        byte[] pdfReceiptArray = receiptGenerator.generatePlanPaymentReceipt(payment);
        String receiptUrl = awsService.uploadPaymentReceipt(pdfReceiptArray, payment.getPaymentId());

        // ✅ Send mail asynchronously
        try {
            webClientService
                    .sendUpdateBymMailWithAttachment(pdfReceiptArray, payment, plan.getDuration(), dto.getUserMail());
        } catch (IOException e) {
            log.warn("Dto failed to send to the user {} due to {}",dto.getUserMail(),e.getMessage());
            throw new EmailSendFailedException
                    ("Unable to send email due to internal servicer error please check your UI to download receipt");
        }

        // ✅ Update plan members count
        plan.setMembersCount(plan.getMembersCount() + 1);
        planRepository.save(plan);

        // ✅ Save payment updates
        payment.setReceiptUrl(receiptUrl);
        paymentRepository.save(payment);

        log.info("Payment confirmed and receipt generated: {}", receiptUrl);
        return receiptUrl;
    }

    private Double calculateDiscountedAmount(PlanCuponCode cuponCode, Double amount) {
        if (cuponCode == null) return amount;
        double discount = (cuponCode.getPercentage() / 100) * amount;
        return amount - discount;
    }

}
