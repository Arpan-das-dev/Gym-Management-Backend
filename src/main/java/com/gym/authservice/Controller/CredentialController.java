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
public class CredentialController {

    private final CredentialService credentialService;

    @PostMapping("forgotPassword")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword
            (@Valid @RequestBody ForgotPasswordRequestDto requestDto){
        ForgotPasswordResponseDto responseDto = credentialService.forgotPassword(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("resetPassword")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword
            (@Valid @RequestBody ResetPasswordRequestDto requestDto){
        ResetPasswordResponseDto responseDto = credentialService.resetPassword(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("changePassword")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto){
         credentialService.changePassword(requestDto);
         return  ResponseEntity.status(HttpStatus.OK).body("Password changed Successfully");
    }

}
