package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.ChangePasswordRequestDto;
import com.gym.authservice.Dto.Request.ForgotPasswordRequestDto;
import com.gym.authservice.Dto.Request.ResetPasswordRequestDto;
import com.gym.authservice.Dto.Response.ForgotPasswordResponseDto;
import com.gym.authservice.Dto.Response.ResetPasswordResponseDto;
import com.gym.authservice.Service.CredentialService;
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
 * This controller manages user credential operations, including password management functionalities.
 * It provides endpoints for users to change their passwords, initiate password recovery (forgot password),
 * and reset their passwords using a verification process.
 * The controller utilizes CredentialService to handle the underlying business logic for these operations.
 */
public class CredentialController {

    private final CredentialService credentialService;

    /*
     * Endpoint to handle forgot password requests.
     * Accepts a ForgotPasswordRequestDto containing user identification details and returns a ForgotPasswordResponseDto.
     */
    @PostMapping("forgotPassword")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword
            (@Valid @RequestBody ForgotPasswordRequestDto requestDto){
        ForgotPasswordResponseDto responseDto = credentialService.forgotPassword(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /*
     * Endpoint to handle password reset requests.
     * Accepts a ResetPasswordRequestDto containing the new password and verification details, and returns a ResetPasswordResponseDto.
     */
    @PostMapping("resetPassword")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword
            (@Valid @RequestBody ResetPasswordRequestDto requestDto){
        ResetPasswordResponseDto responseDto = credentialService.resetPassword(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /*
     * Endpoint to handle password change requests.
     * Accepts a ChangePasswordRequestDto containing the current and new passwords.
     * Returns a success message upon successful password change.
     */
    @PostMapping("changePassword")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto){
         credentialService.changePassword(requestDto);
         return  ResponseEntity.status(HttpStatus.OK).body("Password changed Successfully");
    }

}
