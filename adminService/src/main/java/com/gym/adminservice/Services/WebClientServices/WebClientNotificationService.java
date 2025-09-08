package com.gym.adminservice.Services.WebClientServices;

import com.gym.adminservice.Dto.Responses.ApproveEmailNotificationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service

/*
 * this service class will handle all the webclient calls to the notification
 * service
 * and also it will handle the asynchronous calls
 * later we will add kafka here for sending email notifications for bettet and
 * seamless performance boosting
 */
public class WebClientNotificationService {

    // get the notification service url from the application properties file
    @Value("${app.admin_notificationService.url}")
    private final String adminNotificationService_URL;
    private final WebClient.Builder webClient;


    public WebClientNotificationService(@Value("${app.admin_notificationService.url}") String adminNotificationService_URL, WebClient.Builder webClient) {
        this.adminNotificationService_URL = adminNotificationService_URL;
        this.webClient = webClient;
    }

    /*
     * this method send request to the notification service to publish the approval
     * email notification
     * when the admin approves the request
     */
    @Async
    public void sendApproveMail(ApproveEmailNotificationDto notificationDto) {
        String url = adminNotificationService_URL + "/approved";
        sendAsyncNotification(url, notificationDto);
    }

    /*
     * this method send request to the notification service to publish the declined
     * email notification
     * when the admin declines the request
     */
    @Async
    public void sendDeclinedMail(ApproveEmailNotificationDto notificationDto) {
        String url = adminNotificationService_URL + "/declined";
        sendAsyncNotification(url, notificationDto);
    }

    /*
     * generic method to send the request asynchronously via webclient to the given
     * url with the given body to notification service to publish the email
     * notification
     */
    private void sendAsyncNotification(String url, Object body) {
        webClient.build().post()
                .uri(url)
                .bodyValue(body)
                .retrieve().toBodilessEntity().subscribe(
                        success -> System.out.println("API request  sent successfully to " + url),
                        error -> System.out
                                .println("Failed to send API request to " + url + ": " + error.getMessage()));
    }
}
