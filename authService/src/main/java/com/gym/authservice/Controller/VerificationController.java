package com.gym.authservice.Controller;


import com.gym.authservice.Dto.Request.EmailVerificationRequestDto;
import com.gym.authservice.Dto.Request.PhoneVerificationRequestDto;
import com.gym.authservice.Dto.Response.GenericResponseDto;
import com.gym.authservice.Service.VerificationService;
import jakarta.validation.Valid;
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
 * REST controller for sending and verifying OTPs
 * for user email addresses and phone numbers.
 */
public class VerificationController {

    private final VerificationService verificationService;

    /**
     * Sends an OTP to the specified email address.
     *
     * @param email target email address
     * @param name  recipient name (for personalization)
     * @return HTTP 200 with generic success payload
     */
    @PostMapping("emailVerification/{email}/{name}")
    public ResponseEntity<GenericResponseDto> sendEmailOtp(@PathVariable String email,
                                                           @PathVariable String name) {
        log.info("Request received to send email OTP [email={}, name={}]", email, name);
        verificationService.sendEmailOtp(email, name);
        // Do not log the OTP or any sensitive data.
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDto("Email OTP sent successfully"));
    }

    /**
     * Sends an OTP to the specified phone number.
     *
     * @param phone target phone number in E.164 or agreed format
     * @param name  recipient name (for personalization)
     * @return HTTP 200 with generic success payload
     */
    @PostMapping("phoneVerification/{phone}/{name}")
    public ResponseEntity<GenericResponseDto> sendPhoneOtp(@PathVariable String phone,
                                                           @PathVariable String name) {
        log.info("Request received to send phone OTP [phone={}, name={}]", phone, name);
        verificationService.sendPhoneOtp(phone, name);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDto("Phone OTP sent successfully"));
    }

    /**
     * Verifies an email OTP.
     *
     * @param requestDto payload containing email and OTP
     * @return HTTP 202 if verified, 400 otherwise
     */
    @PostMapping("verifyEmail")
    public Mono<ResponseEntity<GenericResponseDto>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequestDto requestDto) {

        log.info("Email OTP verification requested [email={}]", requestDto.getEmail());

        return verificationService.verifyEmail(requestDto.getEmail(), requestDto.getOtp())
                .map(isVerified -> {
                    if (isVerified) {
                        log.info("Email OTP verification succeeded [email={}]", requestDto.getEmail());
                        return ResponseEntity
                                .status(HttpStatus.ACCEPTED)
                                .body(new GenericResponseDto("Email verified successfully"));
                    }
                    log.warn("Email OTP verification failed [email={}]", requestDto.getEmail());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new GenericResponseDto("Invalid OTP"));
                })
                .onErrorResume(ex -> {
                    // Log full exception for operators, but return generic message to client.
                    log.error("Error during email OTP verification [email={}]", requestDto.getEmail(), ex);
                    return Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.BAD_REQUEST)
                                    .body(new GenericResponseDto("Email verification failed"))
                    );
                });
    }

    /**
     * Verifies a phone OTP.
     *
     * @param requestDto payload containing phone and OTP
     * @return HTTP 202 if verified, 400 otherwise
     */
    @PostMapping("verifyPhone")
    public Mono<ResponseEntity<GenericResponseDto>> verifyPhone(
            @Valid @RequestBody PhoneVerificationRequestDto requestDto) {

        log.info("Phone OTP verification requested [phone={}]", requestDto.getPhone());

        return verificationService.verifyPhone(requestDto.getPhone(), requestDto.getOtp())
                .map(isVerified -> {
                    if (isVerified) {
                        log.info("Phone OTP verification succeeded [phone={}]", requestDto.getPhone());
                        return ResponseEntity
                                .status(HttpStatus.ACCEPTED)
                                .body(new GenericResponseDto("Phone number verified successfully"));
                    }
                    log.warn("Phone OTP verification failed [phone={}]", requestDto.getPhone());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new GenericResponseDto("Invalid OTP"));
                })
                .onErrorResume(ex -> {
                    log.error("Error during phone OTP verification [phone={}]", requestDto.getPhone(), ex);
                    return Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.BAD_REQUEST)
                                    .body(new GenericResponseDto("Phone verification failed"))
                    );
                });
    }
}