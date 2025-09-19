package com.gym.adminservice.Services.WebClientServices;

import com.gym.adminservice.Dto.Responses.MemberAssignmentToTrainerResponseDto;
import com.gym.adminservice.Dto.Responses.SignupResponseDto;
import com.gym.adminservice.Dto.Responses.TrainerAssignMentResponseDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    private final WebClient.Builder webClient;

    public WebClientAuthService(@Value("${app.authService.url}") String authServiceAdmin_URL,
                                @Value("${app.memberService.assign_url}") String memberServiceAssignTrainer_URL,
                                WebClient.Builder webClient) {
        this.authServiceAdmin_URL = authServiceAdmin_URL;
        this.memberServiceAssignTrainer_URL = memberServiceAssignTrainer_URL;
        this.webClient = webClient;
    }

    // send the signup details to the auth service asynchronously via webclient for
    // admin role creation
    @Async
    public void sendSignupDetailsAdmin(SignupResponseDto responseDto) {
        String endpoint = authServiceAdmin_URL + "/CreateAdmin";
        sendAsynchronously(endpoint, responseDto);
    }

    // send the signup details to the auth service asynchronously via webclient for
    // member role creation
    @Async
    public void sendSignupDetailsMember(SignupResponseDto responseDto) {
        String endpoint = authServiceAdmin_URL + "/CreateMember";
        sendAsynchronously(endpoint, responseDto);
    }

    // send the signup details to the auth service asynchronously via webclient for
    // trainer role creation
    @Async
    public void sendSignupDetailsTrainer(SignupResponseDto responseDto) {
        String endpoint = authServiceAdmin_URL + "/createTrainer";
        sendAsynchronously(endpoint, responseDto);
    }

    // delete the user from the auth service asynchronously via webclient
    @Async
    public void deleteUser(String identifier) {
        String url = authServiceAdmin_URL + "/delete";
        deleteAsynchronously(url, identifier);
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
    private void deleteAsynchronously(String url, String deleteBy) {
        webClient.build().delete() // define the method as delete
                .uri(uri -> uri // setting the uri
                        .path(url)// setting the path to the url passed
                        .queryParam("identifier", deleteBy)// setting the query param to the identifier passed
                        .build())
                .retrieve().toBodilessEntity()// retrieve the response as a bodiless entity
                .subscribe( // subscribe to the response to handle success and error but asynchronously for
                            // better performance
                        success -> System.out.println("API request  sent successfully to " + url),
                        error -> System.out
                                .println("Failed to send API request to " + url + ": " + error.getMessage()));
    }

    // send the approval status to the auth service asynchronously via webclient so
    // that the auth service can update the user status accordingly
    public void sendApproval(String email, boolean approval) {
        String url = authServiceAdmin_URL + "/approve";
        webClient.build().post() // define the method as post
                .uri(uri -> uri
                        .path(url) // setting the path to the url passed
                        .queryParam("email", email)// setting the query param to the email passed
                        .queryParam("approve", approval)// setting the query param to the approval status passed
                        .build())
                .retrieve().toBodilessEntity().subscribe( // retrieve the response as a bodiless entity
                        success -> System.out.println("API request  sent successfully to " + url),
                        error -> System.out
                                .println("Failed to send API request to " + url + ": " + error.getMessage()));
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

    @Async
    public void sendDtoForAssignTrainerToMember(TrainerAssignMentResponseDto responseDto) {
        webClient.build().post()
                .uri(memberServiceAssignTrainer_URL)
                .bodyValue(responseDto)
                .retrieve()
                .toBodilessEntity()
                .subscribe(success -> log.info("Dto send to {}", "MemberService"),
                        error -> log.error("Unable to send dto to {} {}", "MemberService", error.getMessage()));
    }

    public void sendDtoForAssignMemberToTrainer(MemberAssignmentToTrainerResponseDto memberResponseDto) { 
        /** * this method will be defined later when the trainer service will be created */

    }
}
