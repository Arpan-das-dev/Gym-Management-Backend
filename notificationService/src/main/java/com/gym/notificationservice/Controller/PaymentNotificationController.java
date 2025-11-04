package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PlanReceiptRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("${plan-paymentService.url}")
@RequiredArgsConstructor
public class PaymentNotificationController {

    @PostMapping(value = "/buyPlan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendPaymentReceipt(
            @RequestPart("attachment") MultipartFile attachment,
            @RequestPart("response") PlanReceiptRequestDto requestDto) {

        log.info("Received PDF receipt ({} bytes) for user {}", attachment.getSize(), requestDto.getUserName());
    }
}
