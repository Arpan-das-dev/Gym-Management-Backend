package com.gym.planService.Controllers;

import com.gym.planService.Dtos.OrderDtos.Requests.PlanPaymentRequestDto;

import com.gym.planService.Dtos.OrderDtos.Requests.ConfirmPaymentDto;
import com.gym.planService.Dtos.OrderDtos.Wrappers.AllRecentTransactionsResponseWrapperDto;
import com.gym.planService.Dtos.PlanDtos.Responses.GenericResponse;
import com.gym.planService.Services.PaymentService.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Validated
@RestController
@RequestMapping("${payment-service.Base_Url.Pay}")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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

    @GetMapping("/admin/getAllTransaction")
    public ResponseEntity<AllRecentTransactionsResponseWrapperDto> getRecentPayment(
            @RequestParam(required = false) String searchBy,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam int pageNo,
            @RequestParam int pageSize
    ) {
        System.out.println("➡ Controller: getAllTransaction() at " + LocalDateTime.now().format(formatter));
        pageNo = Math.max(pageNo, 0);
        pageSize = pageSize< 0 ? 12 : pageSize;
        log.info(
                "Admin Transaction Request | page={} | size={} | searchBy='{}' | sortBy='{}' | direction='{}'",
                pageNo, pageSize, searchBy, sortBy, sortDirection
        );
        AllRecentTransactionsResponseWrapperDto response = paymentService
                .getPaginatedRecentTransactionWithSort(searchBy,sortBy,sortDirection,pageNo,pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
