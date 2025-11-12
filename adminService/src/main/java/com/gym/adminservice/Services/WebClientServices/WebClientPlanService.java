package com.gym.adminservice.Services.WebClientServices;

import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import com.gym.adminservice.Exceptions.Custom.PlanNotFounException;
import com.gym.adminservice.Exceptions.Model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
/*
 * This service class is responsible for communicating with the Plan Service
 * using WebClient to perform CRUD operations on membership plans(microservice architecture).
 */
public class WebClientPlanService {

    // Injecting value from application.properties file for Plan Service admin URL and public URL
    @Value("${app.admin.planService}")
    private final String planServiceAdmin_URL;
    // Injecting value from application.properties file for Plan Service public URL
    @Value("${app.planService.public_url}")
    private final String planServicePublic_URL;
    private final WebClient.Builder webclient;

    // Constructor to initialize the Plan Service URLs and WebClient builder
    public WebClientPlanService(@Value("app.admin.planService") String planServiceAdmin_URL,
                                WebClient.Builder webclient,
                                @Value("${app.planService.public_url}") String planServicePublic_URL) {
        this.planServiceAdmin_URL = planServiceAdmin_URL;
        this.webclient = webclient;
        this.planServicePublic_URL = planServicePublic_URL;
    }


    @Async
    public CompletableFuture<String>  sendCreationToPlanService(Object body) {
        String url = planServiceAdmin_URL + "addPlan";
       return postAsynchronously(url, body);
    }


    public List<CreationResponseDto> getAllPlansFromPlanService() {
        return webclient.build().get()
                .uri(planServicePublic_URL + "getPlans")
                .retrieve().bodyToFlux(CreationResponseDto.class)
                .collectList().block();
    }

    @Async
    public CompletableFuture<String> sendUpdateCreationToPlanService(String id, Object body) {
        String url = planServiceAdmin_URL + "updatePlan";
       return putAsynchronously(url,body,id);
    }

    @Async
    public CompletableFuture<String> sendDeletionRequestById(String id) {
        String URL = planServiceAdmin_URL + "deletePlan"+"?id="+id;
        return webclient.build().delete()
                .uri(URL)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class); // success response
                    } else {
                        // parse error body to ErrorResponse and propagate message
                        return response.bodyToMono(ErrorResponse.class)
                                .flatMap(errorResponse -> Mono.error(
                                        new PlanNotFounException(errorResponse.getMessage())
                                ));
                    }
                })
                .toFuture();
    }


    private CompletableFuture<String> postAsynchronously(String url, Object body) {
        return webclient.build().post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(s -> System.out.println("Successfully sent Api request"))
                .doOnError(e -> System.out.println("Failed to send Api request: " + e.getMessage()))
                .toFuture();
    }

    private CompletableFuture<String>  putAsynchronously(String url, Object body, String id) {
        String endpoint = url+"?id="+id;
        return webclient.build().put()
                .uri(endpoint)
                .bodyValue(body)
                .retrieve().bodyToMono(String.class)
                .doOnSuccess(s->log.info("Request sent to {}",endpoint))
                .doOnError(e->log.warn("Failed to send request to {} due to ::{}",
                        endpoint,e.getCause().toString()))
                .toFuture();
    }

}
