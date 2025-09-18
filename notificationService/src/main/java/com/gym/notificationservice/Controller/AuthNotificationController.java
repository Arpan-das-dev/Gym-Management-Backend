package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.AuthNotificationRequests.EmailOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.PhoneOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.SendCredentialRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.WelcomeRequestDto;
import com.gym.notificationservice.Service.AuthNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${authService.notification.url}")
@Validated
@RequiredArgsConstructor
public class AuthNotificationController {

    private final AuthNotificationService authNotificationService;

    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcome(@Valid @RequestBody WelcomeRequestDto requestDto) {
        authNotificationService.sendWelcomeMail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("email sent successfully");
    }

    @PostMapping("/welcome-credentials")
    public ResponseEntity<String> sendCredentials(@Valid @RequestBody SendCredentialRequestDto requestDto) {
        authNotificationService.SendCredentials(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("email sent successfully");
    }

    @PostMapping("/emailOtp")
    public ResponseEntity<String> sendEmailOtp(@Valid @RequestBody EmailOtpRequestDto requestDto) {
        authNotificationService.sendVerifyEmail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("email sent successfully");
    }

    @PostMapping("/phoneOtp")
    public ResponseEntity<String> sendPhoneOtp(@Valid @RequestBody PhoneOtpRequestDto requestDto) {
        authNotificationService.sendVerifyPhone(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("message sent successfully");
    }
    @PostMapping("/passwordReset")
    public ResponseEntity<String> sendResetPasswordMail(@Valid @RequestBody EmailOtpRequestDto requestDto){
        authNotificationService.sendResetPasswordEmail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("message sent successfully");
    }
}
