package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.LogiInRequestDto;
import com.gym.authservice.Dto.Response.LoginResponseDto;
import com.gym.authservice.Service.LogiInService;
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
public class LoginController {

    private final LogiInService logiInService;

    @PostMapping("login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LogiInRequestDto requestDto){
        LoginResponseDto responseDto = logiInService.login(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    @PostMapping("delete/verify")
    public ResponseEntity<Boolean> deleteAccountVerification (@Valid @RequestBody LogiInRequestDto requestDto){
        Boolean response = logiInService.verifyBeforeDelete(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

