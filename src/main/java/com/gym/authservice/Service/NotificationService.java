package com.gym.authservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final WebClient.Builder webClient;
    private static final String BASE_URL = "http://localhost:8081/fitStudio/auth/notification";

    @Async
    public void sendEmailOtp(Object emailPayload) {
        sendAsyncNotification("/emailOtp", emailPayload);
    }

    @Async
    public void sendPhoneOtp(Object phonePayload) {
        sendAsyncNotification("/phoneOtp", phonePayload);
    }

    @Async
    public void sendPasswordReset(Object passwordPayload) {
        sendAsyncNotification("/passwordReset", passwordPayload);
    }

    @Async
    public void sendWelcome(Object welcomePayload) {
        sendAsyncNotification("/welcome",welcomePayload);
    }

    private void sendAsyncNotification(String endpoint, Object payload) {
        webClient.build().post()
                .uri(BASE_URL+endpoint)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        success -> System.out.println("Notification sent successfully to " + endpoint),
                        error -> System.err.println("Failed to send notification to " + endpoint + ": " + error.getMessage())
                );
    }


}
