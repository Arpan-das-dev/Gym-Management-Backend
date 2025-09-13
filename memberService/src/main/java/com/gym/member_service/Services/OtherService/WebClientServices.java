package com.gym.member_service.Services;

import com.gym.member_service.Model.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WebClientServices {

    @Value("${app.plan-service.Base_URl}")
    private final String  PlanService_URL;
    private final WebClient.Builder webClient;

    public WebClientServices( @Value("${app.plan-service.Base_URl}") String planService_URL,
                              WebClient.Builder webClient)
    {
        PlanService_URL = planService_URL;
        this.webClient = webClient;
    }


    public void sendAlertMessage(Member member) {

    }

    public void sendExpiredMessage(Member member) {
    }

    public void sendFrozenMessage(Member member) {
    }
}
