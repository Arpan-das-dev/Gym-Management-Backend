package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${authService.base_url}")
@Validated
/*
 * This controller handles user sign-up operations, including sending OTPs via email and phone.
 * It provides endpoints for user registration and OTP delivery.
 * The controller uses SignUpService to process the sign-up logic and manage OTPs for verification.
 */
public class SignUpController {

    private final SignUpService signUpService;

    /*
     * Endpoint to handle user sign-up requests.
     * Accepts a SignupRequestDto containing user details and returns a SignUpResponseDto upon successful registration.
     */
    @PostMapping("signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        return ResponseEntity.ok(signUpService.signUp(requestDto));
    }



}
