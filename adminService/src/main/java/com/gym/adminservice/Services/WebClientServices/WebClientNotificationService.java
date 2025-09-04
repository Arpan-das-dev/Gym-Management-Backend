package com.gym.adminservice.Services;

import com.gym.adminservice.Dto.Responses.ApproveEmailNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WebClientNotificationService {

    @Value("${app.admin_notificationService.url}")
    private final String adminNotificationService_URL;
    private final WebClient.Builder webClient;

    @Async
    public void sendApproveMail(ApproveEmailNotificationDto notificationDto){
        String url = adminNotificationService_URL+"/approved";
        sendAsyncNotification(url,notificationDto);
    }

    @Async
    public void sendDeclinedMail(ApproveEmailNotificationDto notificationDto){
        String url = adminNotificationService_URL+"/declined";
        sendAsyncNotification(url,notificationDto);
    }

    private void sendAsyncNotification(String url, Object body) {
        webClient.build().post()
                .uri(url)
                .bodyValue(body)
                .retrieve().toBodilessEntity().subscribe(
                        success -> System.out.println("API request  sent successfully to " + url),
                        error -> System.out.println("Failed to send API request to " + url + ": " + error.getMessage())
                );
    }
}
