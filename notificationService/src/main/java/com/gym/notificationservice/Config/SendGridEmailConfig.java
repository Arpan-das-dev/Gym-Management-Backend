package com.gym.notificationservice.Config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridEmailConfig {

    // API key for SendGrid, from application.properties
    @Value("${app.mail.secret_key}")
    private String sendGridApiKey;

    /**
     * Creates a SendGrid bean that can be injected anywhere in the app.
     */
    @Bean
    public SendGrid sendGrid(){
        return new SendGrid(sendGridApiKey);
    }
}
