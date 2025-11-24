package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.LogiInRequestDto;
import com.gym.authservice.Dto.Response.LoginResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Service.LoginService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("${authService.base_url}")
@RequiredArgsConstructor
@Validated

/*
 * This controller handles user login operations and account deletion verification.
 * It provides endpoints for user authentication and verifying credentials before account deletion.
 * The controller uses LogiInService to process login requests and manage verification logic.
 */
public class LoginController {

    private final LoginService logiInService;

    /*
     * Endpoint to handle user login requests.
     * Accepts a LogiInRequestDto containing user credentials and returns a LoginResponseDto upon successful authentication.
     * If authentication fails, an appropriate error response is returned.
     */
    @PostMapping("login")
    public ResponseEntity<Mono<LoginResponseDto>> login(@Valid @RequestBody LogiInRequestDto requestDto){
        Mono<LoginResponseDto> responseDto = logiInService.login(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /*
     * Endpoint to verify user credentials before account deletion.
     * Accepts a LogiInRequestDto containing user credentials and returns a boolean indicating whether the credentials are valid.
     * This verification step is crucial to ensure that only authorized users can delete their accounts.
     */
    @PostMapping("delete/verify")
    public ResponseEntity<Mono<Boolean>> deleteAccountVerification (@Valid @RequestBody LogiInRequestDto requestDto){
        Mono<Boolean> response = logiInService.verifyBeforeDelete(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/userDetails")
    public ResponseEntity<Mono<SignupDetailsInfoDto>> getUserDetailsByIdentifier(@NotBlank @RequestParam String identifier) {
        log.info("request received to get details for user {}",identifier);
        Mono<SignupDetailsInfoDto> response = logiInService.getUserDetails(identifier);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

