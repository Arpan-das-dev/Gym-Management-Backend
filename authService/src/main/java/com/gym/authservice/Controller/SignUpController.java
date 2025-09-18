package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
<<<<<<< Updated upstream
import org.springframework.http.HttpStatus;
=======

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    /* Endpoint to send OTP via email 
     * Accepts an email address as a path variable and triggers the sending of an OTP to that email.
     * Returns a success message upon successful OTP dispatch.
    */
    @PostMapping("signup/{email}")
    public ResponseEntity<String> sendEmailOtp(@PathVariable String email) {
        signUpService.sendEmailOtp(email);
        return ResponseEntity.status(HttpStatus.OK).body("otp sent successfully via email");
    }
    
    /* Endpoint to send OTP via phone
     * Accepts a phone number as a path variable and triggers the sending of an OTP to that phone number.
     * Returns a success message upon successful OTP dispatch.
    */
    @PostMapping("signup/{phone}")
    public ResponseEntity<String> sendPhoneOtp(@PathVariable String phone) {
        signUpService.sendPhoneOtp(phone);
        return ResponseEntity.status(HttpStatus.OK).body("otp sent successfully via sms");
    }
=======
>>>>>>> Stashed changes
}
