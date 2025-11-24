package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.ChangePasswordRequestDto;
import com.gym.authservice.Dto.Request.ForgotPasswordRequestDto;
import com.gym.authservice.Dto.Request.ResetPasswordRequestDto;
import com.gym.authservice.Dto.Response.EmailOtpNotificationDto;
import com.gym.authservice.Dto.Response.ForgotPasswordResponseDto;
import com.gym.authservice.Dto.Response.ResetPasswordResponseDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UnauthorizedAccessException;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Utils.OtpGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Service layer for managing user credentials including password reset and change operations.
 *
 * <p>This service provides comprehensive business logic for:
 * <ul>
 *   <li>Reactive retrieval of users by email with error handling</li>
 *   <li>Generation and storage of OTPs for password reset notifications</li>
 *   <li>Verification of old password correctness before password reset</li>
 *   <li>Secure password updates with encryption</li>
 *   <li>Validation of user verification status before password changes</li>
 * </ul>
 *
 * <p>The service implements clear logging strategies for monitoring request lifecycle,
 * success, warnings (e.g., same old/new password), and error conditions.
 *
 * <p>Transactional annotations ensure data consistency during updates.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class CredentialService {

    private final SignedUpsRepository signedUpsRepository;
    private final OtpGenerationUtil otpGenerationUtil;
    private final VerificationService verificationService;
    private final WebClientService notificationService;
    private final PasswordEncoder encoder;

    /**
     * Fetches a user reactively by their email.
     *
     * <p>Throws a {@code UserNotFoundException} if the user does not exist.
     * Logs lookup requests at debug level.
     *
     * @param email user email; must not be null
     * @return Mono emitting the user entity or an error if not found
     * @throws UserNotFoundException if user is not found in repository
     */
    private Mono<SignedUps> findUserByEmail(String email) {
        log.debug("Looking up user by email: {}", email);
        return signedUpsRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new UserNotFoundException("User with email '" + email + "' doesn't exist")
                ));
    }

    /**
     * Initiates password reset by sending OTP to the user's registered email.
     *
     * <p>Generates a 6-digit OTP, securely stores it with a validity of 15 minutes,
     * and triggers an asynchronous notification sending.
     * Logs start and successful OTP sending events.
     *
     * @param requestDto contains user email for OTP sending
     * @return Mono with response indicating OTP dispatch success
     */
    public Mono<ForgotPasswordResponseDto> forgotPassword(ForgotPasswordRequestDto requestDto) {
        log.info("Password reset requested for email: {}", requestDto.getEmail());
        return findUserByEmail(requestDto.getEmail())
                .flatMap(user -> {
                    String otp = otpGenerationUtil.generateOtp(6);
                    log.debug("Generated OTP for email [PROTECTED]");
                    verificationService.StoreEmailOtp(user.getEmail(), otp, 900);
                    notificationService.sendPasswordReset(
                            new EmailOtpNotificationDto(user.getEmail(), otp, user.getFirstName())
                    );
                    log.info("Sent OTP for password reset to registered email: {}", user.getEmail());
                    return Mono.just(new ForgotPasswordResponseDto("OTP sent to registered email"));
                });
    }

    /**
     * Resets user password after verifying old password correctness.
     *
     * <p>Ensures the new password is different from the old password.
     * Applies secure encoding to the new password and persists the update transactionally.
     * Logs password reset lifecycle including warnings for invalid conditions.
     *
     * @param requestDto contains email, old password, and new password
     * @return Mono emitting success response or error
     * @throws RuntimeException if old and new password are the same
     * @throws UnauthorizedAccessException if old password is incorrect
     */
    @Transactional
    public Mono<ResetPasswordResponseDto> resetPassword(ResetPasswordRequestDto requestDto) {
        log.info("Password reset attempt for email: {}", requestDto.getEmail());

        if (requestDto.getOldPassword().equals(requestDto.getNewPassword())) {
            log.warn("New password cannot be the same as old password for email: {}", requestDto.getEmail());
            return Mono.error(new RuntimeException("Old password and new password cannot be the same"));
        }

        return findUserByEmail(requestDto.getEmail())
                .flatMap(user -> {
                    if (!encoder.matches(requestDto.getOldPassword(), user.getPassword())) {
                        log.warn("Incorrect old password provided for email: {}", requestDto.getEmail());
                        return Mono.error(new UnauthorizedAccessException("Incorrect old password"));
                    }
                    user.setPassword(encoder.encode(requestDto.getNewPassword()));
                    log.debug("Encoded new password set for email: {}", requestDto.getEmail());
                    return signedUpsRepository.save(user)
                            .thenReturn(new ResetPasswordResponseDto("Password reset successfully"));
                });
    }

    /**
     * Changes user password if the email is verified.
     *
     * <p>Validates the email verification status before performing password update.
     * Logs request initiation, success, and error states.
     *
     * @param requestDto contains email and new password
     * @throws RuntimeException if user's email is not verified
     */
    @Transactional
    public void changePassword(ChangePasswordRequestDto requestDto) {
        log.info("Password change requested for email: {}", requestDto.getEmail());

        findUserByEmail(requestDto.getEmail())
                .flatMap(user -> {
                    if (!user.isEmailVerified()) {
                        log.warn("Password change rejected for unverified email: {}", requestDto.getEmail());
                        return Mono.error(new RuntimeException("Email is not verified"));
                    }
                    user.setPassword(encoder.encode(requestDto.getPassword()));
                    log.debug("Setting new encoded password for email: {}", requestDto.getEmail());
                    return signedUpsRepository.save(user).then();
                })
                .subscribe(
                        unused -> log.info("Password change successful for email: {}", requestDto.getEmail()),
                        error -> log.error("Password change failed for email: {} with error: {}", requestDto.getEmail(), error.getMessage())
                );
    }
}

