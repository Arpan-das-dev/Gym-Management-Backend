package com.gym.notificationservice.Service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    private final String apiKey;

    public SmsService(@Value("${fast2sms.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Async
    public void sendSms(String to, String message) {
        try {
            String url = "https://www.fast2sms.com/dev/bulkV2";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("route", "t");
            body.add("message", message);
            body.add("language", "english");
            body.add("flash", "0");
            body.add("numbers", to);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            throw new RuntimeException("SMS sending failed", e);
        }
    }
}
