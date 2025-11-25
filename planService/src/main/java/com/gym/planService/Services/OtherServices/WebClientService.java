package com.gym.planService.Services.OtherServices;

import com.gym.planService.Dtos.OrderDtos.Responses.PlanNotificationResponse;
import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDtoForMemberService;
import com.gym.planService.Models.Plan;
import com.gym.planService.Models.PlanPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class WebClientService {

    private final WebClient.Builder webclient;
    private final String notification_service_Base_URL;
    private final String member_Service_Plan_URL;


    public WebClientService(WebClient.Builder webclient,
                            @Value("${app.notification-service.Base_Url}") String notification_service_Base_URL,
                            @Value("${app.member-service.Plan_Url}") String member_Service_Plan_URL
                            ) {
        this.webclient = webclient;
        this.notification_service_Base_URL = notification_service_Base_URL;
        this.member_Service_Plan_URL = member_Service_Plan_URL;
    }

    public void sendUpdateBymMailWithAttachment
            (byte[] pdfReceiptArray, PlanPayment payment,Integer duration, String userMail)
            throws IOException {
        String fileName = "Plan bought "+payment.getUserName()+" "+ payment.getPaymentDate();
        File attachment = convertFileFromByteArray(pdfReceiptArray,fileName);
        String endpoint = notification_service_Base_URL+"/all/sendAttachment";
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
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("attachment", new FileSystemResource(attachment));

            // DTO fields individually (required for @ModelAttribute binding)
            builder.part("userName", response.getUserName());
            builder.part("userMail", response.getUserMail());
            builder.part("planName", response.getPlanName());
            builder.part("planDuration", response.getPlanDuration());
            builder.part("planPrice", response.getPlanPrice());

            webclient.build()
                    .post()
                    .uri(endpoint)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(
                            success -> log.info("{} :: request sent successfully to {}",
                                    success.getStatusCode(), endpoint),
                            error -> log.error("request failed :: {}", error.getMessage())
                    );

        } catch (Exception e) {
            log.error("Failed to send multipart request: {}", e.getMessage());
        }
    }

    @Async
    public CompletableFuture<String> sendReviewAttachment(byte[] pdfArray) {
        try {
            File file = convertFileFromByteArray(pdfArray,"INVOICE_FOR_YEAR_"+ LocalDate.now().getYear());
           return webclient.build().post()
                    .uri(notification_service_Base_URL)
                    .body(BodyInserters.fromMultipartData("attachment",new FileSystemResource(file)))
                    .retrieve().bodyToMono(String.class)
                   .doOnSuccess(s-> log.info("attachment sent"))
                   .doOnError(e-> log.warn("failed to sent attachment due to {}",e.getMessage()))
                    .toFuture();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Mono<String> askMemberServiceToAppendPlan(Plan plan, String id){
        String URI = member_Service_Plan_URL+"?id="+id;
        long start = System.currentTimeMillis();
        PlanResponseDtoForMemberService responseDtoForMemberService = PlanResponseDtoForMemberService.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .duration(plan.getDuration())
                .build();
        log.info("Preparing to send plan :: {} to member service",plan.getPlanName());
        return webclient.build()
                .post()
                .uri(URI)
                .bodyValue(responseDtoForMemberService)
                .retrieve().bodyToMono(String.class)
                .doOnSuccess(s->log.info("Successfully send to member-service -> {} in :: {} ms"
                        ,URI, System.currentTimeMillis()-start))
                .doOnError(e-> log.warn("failed to send to member-service -> {} due to:: {}",
                        URI,e.getCause().toString()));

    }

}
