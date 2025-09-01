package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${authService.base_url}")
@Validated
public class SignUpController {

    private final SignUpService signUpService;

    @PostMapping("signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        return ResponseEntity.ok(signUpService.signUp(requestDto));
    }
}
