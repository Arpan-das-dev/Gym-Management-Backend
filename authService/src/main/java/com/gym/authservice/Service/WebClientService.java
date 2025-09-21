package com.gym.authservice.Service;

import com.gym.authservice.Dto.Response.MemberCreationResponseDto;
import com.gym.authservice.Dto.Response.TrainerCreationResponseDto;
import com.gym.authservice.Entity.SignedUps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class WebClientService {
    private final WebClient.Builder webClient;
    @Value("${authentication.notification}")
    private final String BASE_URL;

    @Value("${app.memberService.createMemberUrl}")
    private final String memberService_CREATE_URL;
    @Value("${app.trainerService.createTrainer.Url}")
    private final String trainerService_CREATE_URL;


    public WebClientService(WebClient.Builder webClient,
                            @Value("${authentication.notification}")   String BASE_URL,
                            @Value("${app.memberService.createMemberUrl}") String memberService_CREATE_URL,
                            @Value("${app.trainerService.createTrainer.Url}") String trainerService_CREATE_URL)
    {
        this.webClient = webClient;
        this.BASE_URL = BASE_URL;
        this.memberService_CREATE_URL = memberService_CREATE_URL;
        this.trainerService_CREATE_URL = trainerService_CREATE_URL;
    }

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
        webClient.build().post()
                .uri(BASE_URL + endpoint)
                .bodyValue(payload)
                .retrieve().toBodilessEntity()
                .subscribe(
                        success -> System.out.println("Notification sent successfully to " + endpoint),
                        error -> System.err.println("Failed to send notification to " + endpoint + ": " + error.getMessage()));
    }

    @Async
    public void sendTrainerServiceToCreateNewTrainer(SignedUps user) {
        TrainerCreationResponseDto responseDto = TrainerCreationResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .email(user.getLastName()).phone(user.getLastName())
                .gender(user.getGender())
                .joinDate(user.getJoinDate())
                .build();
        webClient.build().post()
                .uri(trainerService_CREATE_URL)
                .bodyValue(responseDto)
                .retrieve().toBodilessEntity()
                .subscribe(
                        success-> log.info("{} Successfully sent dto to {} ",success.getStatusCode(), trainerService_CREATE_URL),
                        error-> log.error("unable to send {} {}",trainerService_CREATE_URL,error.getMessage())
                );
    }

    @Async
    public void sendMemberServiceToCreateNewMember(SignedUps user) {
        MemberCreationResponseDto responseDto = MemberCreationResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .joinDate(user.getJoinDate())
                .build();
        webClient.build().post()
                .uri(memberService_CREATE_URL)
                .bodyValue(responseDto)
                .retrieve().toBodilessEntity()
                .subscribe(
                        success-> log.info("{} Successfully send to {}",success.getStatusCode(),memberService_CREATE_URL),
                        error->log.error("Unable to send dto to {}",error.getMessage())
                );
    }
}
