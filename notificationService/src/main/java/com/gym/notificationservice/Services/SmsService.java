package com.gym.notificationservice.Services;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    private final String apiKey;

    public SmsService(@Value("${fast2sms.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }


    private final RestTemplate restTemplate = new RestTemplate();
    public void sendOtp(String mobileNumber, String otp) {

        String url = "https://www.fast2sms.com/dev/bulkV2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("route", "v3");
        body.put("sender_id", "XXXXX");     // your DLT sender ID
        body.put("message", "123456");      // your DLT template ID
        body.put("variables_values", otp);
        body.put("numbers", mobileNumber);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(url, entity, String.class);

        System.out.println("Fast2SMS Response = " + response);
    }
}

