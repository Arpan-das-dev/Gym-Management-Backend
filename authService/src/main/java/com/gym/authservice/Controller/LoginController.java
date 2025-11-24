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

/**
 * Controller handling user authentication and credential verification operations.
 *
 * <p>This controller provides REST endpoints for:
 * <ul>
 *   <li>User login authentication with JWT token issuance</li>
 *   <li>Verification of user credentials before account deletion</li>
 *   <li>Retrieval of user profile details by identifier</li>
 * </ul>
 *
 * <p>Utilizes LogiInService for business logic execution and demonstrates
 * comprehensive logging for request tracing and error monitoring.
 *
 * Endpoints accept and return reactive types to support asynchronous processing.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("${authService.base_url}")
@RequiredArgsConstructor
@Validated

public class LoginController {

    private final LoginService logiInService;

    /**
     * Handles user login requests reactively.
     *
     * <p>Validates user credentials and returns a JWT token and role upon success.
     * Logs incoming requests and uses reactive Mono for async response.
     *
     * @param requestDto login request containing user identifier and password
     * @return ResponseEntity wrapping a Mono emitting LoginResponseDto with JWT and role info
     */
    @PostMapping("login")
    public ResponseEntity<Mono<LoginResponseDto>> login(@Valid @RequestBody LogiInRequestDto requestDto) {
        log.info("Login request received for identifier: {}", requestDto.getIdentifier());
        Mono<LoginResponseDto> responseDto = logiInService.login(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     * Verifies user credentials before authorizing account deletion.
     *
     * <p>Validates password correctness for security-sensitive operations.
     * Logs verification requests and returns boolean Mono indicating success.
     *
     * @param requestDto login request with identifier and password for verification
     * @return ResponseEntity wrapping a Mono emitting a boolean flag for verification success
     */
    @PostMapping("delete/verify")
    public ResponseEntity<Mono<Boolean>> deleteAccountVerification(@Valid @RequestBody LogiInRequestDto requestDto) {
        log.info("Delete verification requested for identifier: {}", requestDto.getIdentifier());
        Mono<Boolean> response = logiInService.verifyBeforeDelete(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves user profile details by their identifier.
     *
     * <p>Logs requests and returns reactive response with user details DTO.
     *
     * @param identifier user's email or ID; must not be blank
     * @return ResponseEntity wrapping a Mono emitting user's profile data
     */
    @GetMapping("/userDetails")
    public ResponseEntity<Mono<SignupDetailsInfoDto>> getUserDetailsByIdentifier(@NotBlank @RequestParam String identifier) {
        log.info("User detail request received for identifier: {}", identifier);
        Mono<SignupDetailsInfoDto> response = logiInService.getUserDetails(identifier);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

