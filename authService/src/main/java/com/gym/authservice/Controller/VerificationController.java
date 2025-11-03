package com.gym.authservice.Controller;


import com.gym.authservice.Dto.Request.EmailVerificationRequestDto;
import com.gym.authservice.Dto.Request.PhoneVerificationRequestDto;
import com.gym.authservice.Service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    @PostMapping("emailVerification/{email}/{name}")
    public ResponseEntity<String> sendEmailOtp(@PathVariable String email,@PathVariable String name) {
        verificationService.sendEmailOtp(email,name);
        return ResponseEntity.status(HttpStatus.OK).body("email otp send successfully");
    }

    @PostMapping("phoneVerification/{phone}/{name}")
    public ResponseEntity<String> sendPhoneOtp(@PathVariable String phone,@PathVariable String name) {
        verificationService.sendPhoneOtp(phone,name);
        return ResponseEntity.status(HttpStatus.OK).body("phone otp send successfully");
    }

    @PostMapping("verifyEmail")
    public Mono<ResponseEntity<String>> verifyEmail(@Valid @RequestBody EmailVerificationRequestDto requestDto) {
        return verificationService.verifyEmail(requestDto.getEmail(), requestDto.getOtp())
                .map(isVerified -> {
                    if (isVerified) {
                        return ResponseEntity.status(HttpStatus.ACCEPTED)
                                .body("Email verified successfully");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Invalid OTP");
                    }
                })
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(e.getMessage()))
                );
    }

    /* Endpoint to verify phone using OTP.
     * Accepts a PhoneVerificationRequestDto containing the phone number and OTP.
     * Returns a success message if the OTP is valid, otherwise returns an error message.
     */
    @PostMapping("verifyPhone")
    public Mono<ResponseEntity<String>> verifyPhone(@Valid @RequestBody PhoneVerificationRequestDto requestDto) {
        return verificationService.verifyPhone(requestDto.getPhone(), requestDto.getOtp())
                .map(isVerified -> {
                    if (isVerified) {
                        return ResponseEntity
                                .status(HttpStatus.ACCEPTED)
                                .body("Phone number verified successfully");
                    } else {
                        return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body("Invalid OTP");
                    }
                });
    }
}
