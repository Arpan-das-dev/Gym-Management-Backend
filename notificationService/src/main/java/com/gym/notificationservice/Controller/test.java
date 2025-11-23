package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Services.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test-me")
@RequiredArgsConstructor
public class test {

    private final SmsService smsService;
    @PostMapping("/now")
    public void sendSms(){
        smsService.sendOtp("8145415374","BApKeLauDe");
    }
}
