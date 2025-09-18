package com.gym.authservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WebClientService {
    private final WebClient.Builder webClient;
    @Value("${authentication.notification}")
    private final String BASE_URL;

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
        sendAsyncNotification("/welcome", welcomePayload);
    }

    @Async
    public void sendCredentials(Object credentials) {
        sendAsyncNotification("/welcome-credentials", credentials);
    }

    private void sendAsyncNotification(String endpoint, Object payload) {
        webClient.build().post().uri(BASE_URL + endpoint).bodyValue(payload)
                .retrieve().toBodilessEntity()
                .subscribe(
                        success -> System.out.println("Notification sent successfully to " + endpoint),
                        error -> System.err.println("Failed to send notification to " + endpoint + ": " + error.getMessage()));
    }


}
