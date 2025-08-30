package com.gym.authservice.Security.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
@Bean
   public WebClient.Builder WebClientBuilder(){
    return WebClient.builder();
}
}
