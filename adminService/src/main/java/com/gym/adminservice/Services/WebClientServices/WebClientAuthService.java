package com.gym.adminservice.Services;

import com.gym.adminservice.Dto.Responses.MemberResponseDto;
import com.gym.adminservice.Dto.Responses.SignupResponseDto;
import com.gym.adminservice.Dto.Responses.TrainerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WebClientAuthService {
    @Value("${app.authService.url}")
    private final String authServiceAdmin_URL;
    private final WebClient.Builder webClient;

    @Async
    public void sendSignupDetailsMember(SignupResponseDto responseDto) {
        String endpoint = authServiceAdmin_URL+"/CreateMember";
        sendAsynchronously(endpoint, responseDto);
    }
    @Async
    public void sendMemberDetails(MemberResponseDto memberResponseDto) {
        System.out.println("hello honey bunny");
    }
    @Async
    public void deleteMember(String email) {
        System.out.println("add url later on");
    }

    
    @Async
    public void sendSignupDetailsTrainer(SignupResponseDto responseDto){
        String endpoint =authServiceAdmin_URL+"/createTrainer";
        sendAsynchronously(endpoint,responseDto);
    }
    @Async
    public void sendTrainerDetails(TrainerResponseDto trainerResponseDto) {
        System.out.println("hello honey bunny");
    }
    @Async
    public void deleteTrainer( String email) {
        System.out.println("add url later on");
        // deleteAsynchronously(url,email);
    }

    
    @Async
    public void deleteUser(String identifier) {
        String url = authServiceAdmin_URL+"/delete";
        deleteAsynchronously(url,identifier);

    }
    
    private void sendAsynchronously(String endpoint, Object body) {
        webClient.build().post()
                .uri(endpoint)
                .bodyValue(body)
                .retrieve().toBodilessEntity()
                .subscribe(
                        success -> System.out.println("Notification sent successfully to " + endpoint),
                        error -> System.out.println("Failed to send notification to " + endpoint + ": " + error.getMessage())
                );
    }
    
    private void deleteAsynchronously (String url,String deleteBy){
        webClient.build().delete()
                .uri(uri->uri
                        .path(url)
                        .queryParam("identifier", deleteBy)
                        .build())
                .retrieve().toBodilessEntity()
                .subscribe(
                        success -> System.out.println("API request  sent successfully to " + url),
                        error -> System.out.println("Failed to send API request to " + url + ": " + error.getMessage())
                );
    }


    public void sendApproval(String email, boolean approval) {
        String url = authServiceAdmin_URL+"/approve";
        webClient.build().post()
                .uri(uri->uri
                        .path(url)
                        .queryParam("email",email)
                        .queryParam("approve",approval)
                        .build())
                .retrieve().toBodilessEntity().subscribe(
                        success -> System.out.println("API request  sent successfully to " + url),
                        error -> System.out.println("Failed to send API request to " + url + ": " + error.getMessage())
                );
    }
}
