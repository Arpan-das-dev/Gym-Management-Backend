package com.gym.planService.Services.OtherServices;

import com.gym.planService.Dtos.OrderDtos.Responses.PlanNotificationResponse;
import com.gym.planService.Models.PlanPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class WebClientService {

    private final WebClient.Builder webclient;
    private final String notification_service_Base_URL;

    public WebClientService(WebClient.Builder webclient,
                            @Value("${app.notification-service.Base_Url}") String notification_service_Base_URL) {
        this.webclient = webclient;
        this.notification_service_Base_URL = notification_service_Base_URL;
    }

    public void sendUpdateBymMailWithAttachment(byte[] pdfReceiptArray, PlanPayment payment,Integer duration, String userMail)
            throws IOException {
        String fileName = "Plan bought "+payment.getUserName()+" "+ payment.getPaymentDate();
        File attachment = convertFileFromByteArray(pdfReceiptArray,fileName);
        String endpoint = notification_service_Base_URL+"/buyPlan";
        PlanNotificationResponse response = PlanNotificationResponse.builder()
                .userName(payment.getUserName())
                .userMail(userMail)
                .planName(payment.getPlanName())
                .planPrice(payment.getPaidPrice())
                .planDuration(duration)
                .build();
        sendAsyncPaymentMail(attachment,endpoint,response);
    }

    private File convertFileFromByteArray(byte[] pdfReceiptArray, String fileName) throws IOException {
        File file = File.createTempFile(fileName , ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(pdfReceiptArray);
        }
        return file;
    }

    @Async
    private void sendAsyncPaymentMail(File attachment, String endpoint, PlanNotificationResponse response) {
        try {
            webclient.build()
                    .post()
                    .uri(endpoint)
                    .body(
                            BodyInserters.fromMultipartData("attachment", new FileSystemResource(attachment))
                                    .with("response", response)
                    )
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(
                            success -> log.info("{} :: dto sent successfully to {}", success.getStatusCode(), endpoint),
                            error -> log.error("dto sending failed due to :: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.error("Failed to send multipart request: {}", e.getMessage());
        }
    }

}
