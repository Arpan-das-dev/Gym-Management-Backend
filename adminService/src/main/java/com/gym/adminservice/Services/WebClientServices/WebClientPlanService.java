package com.gym.adminservice.Services.WebClientServices;

import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

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
    public void sendCreationToPlanService(Object body) {
        String url = planServiceAdmin_URL + "createPlan";
        postAsynchronously(url, body);
    }


    public List<CreationResponseDto> getAllPlansFromPlanService() {
        return webclient.build().get()
                .uri(planServicePublic_URL + "getAll")
                .retrieve().bodyToFlux(CreationResponseDto.class)
                .collectList().block();
    }

    @Async
    public void sendUpdateCreationToPlanService(String id, Object body) {
        String url = planServiceAdmin_URL + "updatePlan";
        putAsynchronously(url,body,id);
    }

    @Async
    public void sendDeletionRequestById(String id) {
        String URL = planServiceAdmin_URL + "delete";
        webclient.build().delete()
                .uri(url -> url
                        .path(URL)
                        .queryParam("id", id)
                        .build())
                .retrieve().toBodilessEntity().subscribe(
                        success -> System.out.println("Successfully sent Api request"),
                        error -> System.out.println("Failed to send Api request")
                );
    }


    private void postAsynchronously(String url, Object body) {
        webclient.build().post()
                .uri(url)
                .bodyValue(body)
                .retrieve().toBodilessEntity().subscribe(
                        success -> System.out.println("Successfully sent Api request"),
                        error -> System.out.println("Failed to send Api request")
                );
    }

    private void putAsynchronously(String url, Object body, String id) {
        webclient.build().put()
                .uri(uri->uri
                        .path(url)
                        .queryParam("id",id)
                        .build())
                .bodyValue(body)
                .retrieve().toBodilessEntity().subscribe(
                        success -> System.out.println("Successfully sent Api request"),
                        error -> System.out.println("Failed to send Api request")
                );
    }

}
