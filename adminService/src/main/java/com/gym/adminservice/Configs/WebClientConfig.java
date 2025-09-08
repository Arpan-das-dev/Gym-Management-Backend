package com.gym.adminservice.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
/*
 * Bean for WebClient to make HTTP requests to other services
 */
    @Bean
    public WebClient.Builder webClient(){
        return WebClient.builder();
    }
}
