package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PaymentFailedDto;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PlanNotificationRequest;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PlanPaymentRequestDto;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Responses.GenericResponseDto;
import com.gym.notificationservice.Services.PaymentNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("${plan-paymentService.url}")
@RequiredArgsConstructor
public class PaymentNotificationController {

    private final PaymentNotificationService notificationService;
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

    @PostMapping(
            value = "/all/sendAttachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendMailWithAttachment(
            @ModelAttribute PlanNotificationRequest request,
            @RequestParam("attachment") MultipartFile multipartFile) {

        log.info("request received to send email with attachment");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(notificationService.sendAttachedMail(request, multipartFile));
    }

    @PostMapping("/all/paymentFailed")
    public ResponseEntity<String> sendMailForFailedPayment(@Valid @RequestBody PaymentFailedDto failedDto){
        log.info("©️©️ request received to send email to [{}] for failed payment",failedDto.getEmailId());
            String response = notificationService.sendFailedPaymentMail(failedDto);
            log.info("sending response for failed payment as [{}]",response);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    @PostMapping("/all/refundFailed")
    public ResponseEntity<String> sendMailForFailedRefund(@Valid @RequestBody PaymentFailedDto failedDto){
        log.info("©️©️ request received to send email to [{}] for failed refund",failedDto.getEmailId());
            String response = notificationService.sendRefundFailedMail(failedDto);
            log.info("sending response for failed refund as [{}]",response);
            return ResponseEntity.status(HttpStatus.OK).body(response);

    }
}

