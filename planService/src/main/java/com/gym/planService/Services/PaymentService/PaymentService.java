package com.gym.planService.Services.PaymentService;

import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;
import com.gym.planService.Exception.Custom.PaymentGatewayException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public String buyPlan( PlanPaymentRequestDto requestDto) {
        log.info("request received for payment of Rs. {}",requestDto.getAmount());
        log.info("cupon code is {}",requestDto.getCuponCode());

        Plan plan = planRepository.findById(requestDto.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("No plan found with ID: " + requestDto.getPlanId()));

        // Fetch coupon (optional)
        PlanCuponCode cuponCode = cuponCodeRepository.findById(requestDto.getCuponCode()).orElse(null);

        // Generate payment ID
        String paymentId = paymentIdGenUtil.generatePaymentId(
                requestDto.getUserId(),
                requestDto.getPlanId(),
                requestDto.getPaymentDate()
        );

        // Calculate payable amount after discount (if any)
        Double finalAmount = calculateDiscountedAmount(cuponCode, requestDto.getAmount());

        // Create Razorpay order
        Order razorOrder;
        try {
            log.info("final amount to be paid ====> {}",finalAmount.toString());
            razorOrder = razorPayService.makePayment(finalAmount.longValue(), requestDto.getCurrency(), paymentId);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for user {}: {}", requestDto.getUserId(), e.getMessage());
            throw new PaymentGatewayException("Unable to initiate payment. Please try again later.");
        }

        // Build payment entity
        PlanPayment payment = PlanPayment.builder()
                .paymentId(paymentId)
                .userName(requestDto.getUserName())
                .userId(requestDto.getUserId())
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .paidPrice(finalAmount)
                .currency(requestDto.getCurrency())
                .paymentMethod("RAZORPAY")
                .paymentStatus("PENDING(TEST)") // updated after webhook success
                .orderId(razorOrder.get("id")) // from Razorpay order response
                .paymentDate(requestDto.getPaymentDate().toLocalDate())
                .paymentMonth(requestDto.getPaymentDate().getMonth().toString())
                .paymentYear(requestDto.getPaymentDate().getYear())
                .transactionTime(LocalDateTime.now())
                .build();

        // Save payment info (initial state)
        String response;
        try{
            byte[] pdfReceiptArray = receiptGenerator.generatePlanPaymentReceipt(payment);
            response = awsService.uploadPaymentReceipt(pdfReceiptArray,payment.getPaymentId());
            webClientService.sendUpdateBymMailWithAttachment(
                    pdfReceiptArray,payment,plan.getDuration(),requestDto.getUserMail());
            payment.setReceiptUrl(response);
            plan.setMembersCount(plan.getMembersCount()+1);
            planRepository.save(plan);
        } catch (Exception e) {
            log.warn("error caused due to {}",e.getMessage());
            e.getCause();
            throw new RuntimeException(e);
        }
        paymentRepository.save(payment);
        log.info("Payment initiated for user: {} | Plan: {} | OrderID: {}",
                requestDto.getUserId(), plan.getPlanName(), razorOrder.get("id"));


        // Return order ID to frontend for Razorpay checkout
        return response == null ? razorOrder.get("id") : response;
    }
    private Double calculateDiscountedAmount(PlanCuponCode cuponCode, Double amount) {
        if (cuponCode == null) return amount;
        double discount = (cuponCode.getPercentage() / 100) * amount;
        return amount - discount;
    }

}
