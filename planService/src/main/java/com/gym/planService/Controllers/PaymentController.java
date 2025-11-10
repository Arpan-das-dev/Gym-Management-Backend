package com.gym.planService.Controllers;

import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;

import com.gym.planService.Dtos.OrderDtos.Requests.ConfirmPaymentDto;
import com.gym.planService.Dtos.PlanDtos.Responses.GenericResponse;
import com.gym.planService.Services.PaymentService.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("${payment-service.Base_Url.Pay}")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/all/createOrder")
    public ResponseEntity<GenericResponse> createOrder(@Valid @RequestBody PlanPaymentRequestDto requestDto) {
        log.info("Request received to create order for Rs. {}", requestDto.getAmount());
        String orderId = paymentService.createOrder(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse(orderId));
    }

    // Step 2: Confirm payment after success
    @PostMapping("/all/confirmPayment")
    public ResponseEntity<GenericResponse> confirmPayment(@RequestBody ConfirmPaymentDto confirmDto) {
        log.info("Confirming payment for orderId: {}", confirmDto.getOrderId());
        String receiptUrl = paymentService.confirmPayment(confirmDto);
        return ResponseEntity.ok(new GenericResponse(receiptUrl));
    }
}
