package com.gym.authservice.Controller;


import com.gym.authservice.Dto.Request.EmailVerificationRequestDto;
import com.gym.authservice.Dto.Request.PhoneVerificationRequestDto;
import com.gym.authservice.Service.VerificationService;
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
@RequestMapping("${authService.base_url}")
@RequiredArgsConstructor
@Validated
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("verifyEmail")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody EmailVerificationRequestDto requestDto) {
        if (verificationService.verifyEmail(requestDto.getEmail(), requestDto.getOtp())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Email verified successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }

    @PostMapping("verifyPhone")
    public ResponseEntity<String> verifyPhone(@Valid @RequestBody PhoneVerificationRequestDto requestDto) {
        boolean verification = verificationService.verifyPhone(requestDto.getPhone(), requestDto.getOtp());
        if (verification) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Phone no verified successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }
}
