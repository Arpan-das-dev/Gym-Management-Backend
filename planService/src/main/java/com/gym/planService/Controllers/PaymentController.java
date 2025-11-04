package com.gym.planService.Controllers;

import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;

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

    @PostMapping("/all/buyPlan")
    public ResponseEntity<GenericResponse> makePayment(@Valid @RequestBody PlanPaymentRequestDto requestDto) {
        log.info("");
        String response = paymentService.buyPlan(requestDto);
        log.info("Serving the response {}",response);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body( new GenericResponse(response));
    }
}
