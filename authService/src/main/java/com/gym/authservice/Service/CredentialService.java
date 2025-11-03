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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final SignedUpsRepository signedUpsRepository;
    private final OtpGenerationUtil otpGenerationUtil;
    private final VerificationService verificationService;
    private final WebClientService notificationService;
    private final PasswordEncoder encoder;

    /**
     * ✅ Common helper method to fetch user reactively by email.
     */
    private Mono<SignedUps> findUserByEmail(String email) {
        return signedUpsRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new UserNotFoundException("User with email '" + email + "' doesn't exist")
                ));
    }

    /**
     * ✅ Sends OTP for password reset via email.
     */
    public Mono<ForgotPasswordResponseDto> forgotPassword(ForgotPasswordRequestDto requestDto) {
        return findUserByEmail(requestDto.getEmail())
                .flatMap(user -> {
                    String otp = otpGenerationUtil.generateOtp(6);
                    verificationService.StoreEmailOtp(user.getEmail(), otp, 900);
                    notificationService.sendPasswordReset(
                            new EmailOtpNotificationDto(user.getEmail(), otp, user.getFirstName())
                    );
                    return signedUpsRepository.save(user)
                            .thenReturn(new ForgotPasswordResponseDto("OTP sent to registered email"));
                });
    }

    /**
     * ✅ Resets user password after OTP verification.
     */
    public Mono<ResetPasswordResponseDto> resetPassword(ResetPasswordRequestDto requestDto) {
        if (requestDto.getOldPassword().equals(requestDto.getNewPassword())) {
            return Mono.error(new RuntimeException("Old password and new password cannot be the same"));
        }

        return findUserByEmail(requestDto.getEmail())
                .flatMap(user -> {
                    if (!encoder.matches(requestDto.getOldPassword(), user.getPassword())) {
                        return Mono.error(new UnauthorizedAccessException("Incorrect old password"));
                    }
                    user.setPassword(encoder.encode(requestDto.getNewPassword()));
                    return signedUpsRepository.save(user)
                            .thenReturn(new ResetPasswordResponseDto("Password reset successfully"));
                });
    }

    /**
     * ✅ Changes password if email is verified.
     */
    public Mono<Void> changePassword(ChangePasswordRequestDto requestDto) {
        return findUserByEmail(requestDto.getEmail())
                .flatMap(user -> {
                    if (!user.isEmailVerified()) {
                        return Mono.error(new RuntimeException("Email is not verified"));
                    }
                    user.setPassword(encoder.encode(requestDto.getPassword()));
                    return signedUpsRepository.save(user).then();
                });
    }
}
