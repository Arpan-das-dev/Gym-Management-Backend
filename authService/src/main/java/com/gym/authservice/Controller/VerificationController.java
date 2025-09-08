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
/*
 * This controller manages the verification of user email addresses and phone numbers using OTPs.
 * It provides endpoints for verifying email and phone OTPs.
 * The controller utilizes VerificationService to handle the verification logic.
 */
public class VerificationController {

    private final VerificationService verificationService;

    /*
     * Endpoint to verify email using OTP.
     * Accepts an EmailVerificationRequestDto containing the email and OTP.
     * Returns a success message if the OTP is valid, otherwise returns an error message.
     */
    @PostMapping("verifyEmail")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody EmailVerificationRequestDto requestDto) {
        if (verificationService.verifyEmail(requestDto.getEmail(), requestDto.getOtp())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Email verified successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }

    /* Endpoint to verify phone using OTP.
     * Accepts a PhoneVerificationRequestDto containing the phone number and OTP.
     * Returns a success message if the OTP is valid, otherwise returns an error message.
     */
    @PostMapping("verifyPhone")
    public ResponseEntity<String> verifyPhone(@Valid @RequestBody PhoneVerificationRequestDto requestDto) {
        boolean verification = verificationService.verifyPhone(requestDto.getPhone(), requestDto.getOtp());
        if (verification) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Phone no verified successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }
}
