package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PlanPaymentRequestDto;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Responses.GenericResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${plan-paymentService.url}")
@RequiredArgsConstructor
public class PaymentNotificationController {

    @PostMapping("/all/createOrder")
    public ResponseEntity<GenericResponseDto> createOrder(@Valid @RequestBody PlanPaymentRequestDto requestDto) {
        log.info("");
        return ResponseEntity.status(200).body(new GenericResponseDto("created"));
    }

    @PostMapping("/all/yearlyRevenue")
    public ResponseEntity<GenericResponseDto> sendInvoiceToAdmin(){
        log.info("");
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponseDto("sent"));
    }
}
