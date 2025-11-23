package com.gym.notificationservice.Config;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailJetConfig {

    private final String api_KEY;
    private final String secret_key;

    public MailJetConfig(@Value("${mailjet.api.key}") String api_KEY,
                         @Value("${mailjet.secret.key}") String secret_key)
    {
        this.api_KEY = api_KEY;
        this.secret_key = secret_key;
    }

    @Bean
    public MailjetClient mailjetClient (){
        ClientOptions options = ClientOptions.builder()
                .apiKey(api_KEY)
                .apiSecretKey(secret_key)
                .build();
        return new MailjetClient(options);
    }
}
