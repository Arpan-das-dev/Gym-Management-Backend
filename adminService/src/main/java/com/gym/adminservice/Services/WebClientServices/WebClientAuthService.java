package com.gym.adminservice.Services.WebClientServices;

import com.gym.adminservice.Dto.Responses.*;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service

/*
 * this service class will handle all the webclient calls to the auth service,
 * and also it will handle the asynchronous calls
 */
public class WebClientAuthService {

    // get the auth service url from the application properties file
    @Value("${app.authService.url}")
    private final String authServiceAdmin_URL;
    @Value("${app.memberService.assign_url}")
    private final String memberServiceAssignTrainer_URL;
    private final String trainerServiceAssignMember_URL;
    private final String trainerServiceRollBackMember_URL;
    private final WebClient.Builder webClient;

    public WebClientAuthService(@Value("${app.authService.url}") String authServiceAdmin_URL,
                                @Value("${app.memberService.assign_url}") String memberServiceAssignTrainer_URL,
                                @Value("${app.trainerService.assign_url}") String trainerServiceAssignMember_URL,
                                @Value("${app.trainerService.rollBack_url}") String  trainerServiceRollBackMember_URL,
                                WebClient.Builder webClient) {
        this.authServiceAdmin_URL = authServiceAdmin_URL;
        this.memberServiceAssignTrainer_URL = memberServiceAssignTrainer_URL;
        this.trainerServiceAssignMember_URL = trainerServiceAssignMember_URL;
        this.trainerServiceRollBackMember_URL = trainerServiceRollBackMember_URL;
        this.webClient = webClient;
    }

    // send the signup details to the auth service asynchronously via webclient for
    // admin role creation
    @Async
    public void sendSignupDetailsAdmin(AdminCreationRequestDto responseDto) {
        String endpoint = authServiceAdmin_URL + "/createAdmin";
        sendAsynchronously(endpoint, responseDto);
    }

    // send the signup details to the auth service asynchronously via webclient for
    // member role creation
    @Async
    public void sendSignupDetailsMember(SignupRequestDto responseDto) {
        String endpoint = authServiceAdmin_URL + "/CreateMember";
        sendAsynchronously(endpoint, responseDto);
    }

    // send the signup details to the auth service asynchronously via webclient for
    // trainer role creation
    @Async
    public void sendSignupDetailsTrainer(SignupRequestDto responseDto) {
        String endpoint = authServiceAdmin_URL + "/createTrainer";
        sendAsynchronously(endpoint, responseDto);
    }

    // delete the user from the auth service asynchronously via webclient
    @Async
    public CompletableFuture<String> deleteUser(String identifier) {
        String url = authServiceAdmin_URL + "/delete";
       return deleteAsynchronously(url, identifier);
    }

    // delete the trainer from the trainer service asynchronously via webclient
    @Async
    public void deleteTrainer(String email) {
        String url = authServiceAdmin_URL + "/deleteTrainer";
        deleteUserFromTheirService(url, email);
    }

    // delete the member from the member service asynchronously via webclient
    @Async
    public void deleteMember(String email) {
        String url = authServiceAdmin_URL + "/deleteMember";
        deleteUserFromTheirService(url, email);
    }

    // generic method to send post request asynchronously via webclient which is
    // used for sending requests to the auth service
    private void sendAsynchronously(String endpoint, Object body) {
        // send the request asynchronously
        webClient.build().post() // define the method as post
                .uri(endpoint) // setting the uri to the endpoint
                .bodyValue(body) // setting the body to the object passed
                .retrieve().toBodilessEntity() // retrieve the response as a bodiless entity
                .subscribe( // subscribe to the response to handle success and error but asynchronously for
                            // better performance
                        success -> System.out.println("Notification sent successfully to " + endpoint),
                        error -> System.out
                                .println("Failed to send notification to " + endpoint + ": " + error.getMessage()));
    }

    // generic method to send delete request asynchronously via webclient which is
    // used for sending requests to the auth service
    private CompletableFuture<String> deleteAsynchronously(String url, String deleteBy) {
        String URI = url+"?identifier="+deleteBy;
       return webClient.build().delete() // define the method as delete
                .uri(URI)
                .retrieve()// retrieve the response as a bodiless entity
               .bodyToMono(String.class).toFuture();
    }

    // send the approval status to the auth service asynchronously via webclient so
    // that the auth service can update the user status accordingly
    public void sendApproval(String email, boolean approval) {
        String url  = "http://localhost:8080/fitStudio/auth/approve";
        ApprovalRequestDto requestDto = ApprovalRequestDto.builder()
                .email(email)
                .approval(approval)
                .build();
        webClient.build()
                .post()
                .uri(url).bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        success -> System.out.println("✅ Approval sent successfully to " + url),
                        error -> System.err.println("❌ Failed to send approval to " + url + ": " + error.getMessage())
                );
    }

    // send delete request to the respective service(trainer/member) to delete the
    // user from their respective service asynchronously via webclient
    public void deleteUserFromTheirService(String url, String email) {
        webClient.build().delete() // define the method as delete
                .uri(uri -> uri
                        .path(url)// setting the path to the url passed
                        .queryParam("email", email) // setting the query param to the email passed
                        .build())
                .retrieve().toBodilessEntity().subscribe( // retrieve the response as a bodiless entity
                        success -> System.out.println("API  Deletion request  sent successfully to " + url),
                        error -> System.out.println("Failed to send API request to " + url + ": " + error.getMessage())
                // subscribe to the response to handle success and error but asynchronously for
                // better performance
                // log the success and error messages
                );
    }

    public Mono<Void> sendDtoForAssignTrainerToMember(TrainerAssignmentResponseDto responseDto) {

        return webClient.build()
                .post()
                .uri(memberServiceAssignTrainer_URL)
                .bodyValue(responseDto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res ->
                        log.info("✔️ Successfully forwarded trainer-assignment DTO to Member-Service"))
                .doOnError(err ->
                        log.error("❌ Failed to send trainer-assignment DTO to Member-Service : {}", err.getMessage()))
                .then();  // convert Mono<ResponseEntity<Void>> to Mono<Void>
    }



    public Mono<String> sendDtoForAssignMemberToTrainer(MemberAssignmentToTrainerResponseDto memberResponseDto) {
        String url = trainerServiceAssignMember_URL+"?trainerId="+memberResponseDto.getTrainerId();
        return webClient.build().post()
                .uri(url)
                .bodyValue(memberResponseDto)
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().is2xxSuccessful()){
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Member Successfully Assigned to trainer")
                                .doOnNext(msg-> log.info("✔️ Trainer service success response: {}", msg));
                    }else {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Admin service returned an error")
                                .flatMap(errorMsg -> {
                                    log.error("Admin-Service error response: {}", errorMsg);
                                    return Mono.error(new DuplicateRequestException(errorMsg));
                                });
                    }
                });


    }
    public Mono<String> RollBackMemberFromTrainerService(String trainerId, String memberId){
        String url = trainerServiceRollBackMember_URL+"?trainerId="+trainerId+"&memberId="+memberId;
        return webClient.build().delete()
                .uri(url)
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().is2xxSuccessful()){
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Member Successfully Deleted From Trainer Service")
                                .doOnNext(msg-> log.info("✔️ Trainer service's response is ::->{}",msg));
                    } else {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Failed To Delete From Trainer Please Delete the member From your Dashboard");
                    }
                });
    }
}
