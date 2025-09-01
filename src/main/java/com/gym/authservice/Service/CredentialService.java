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

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final SignedUpsRepository signedUpsRepository;
    private final OtpGenerationUtil otpGenerationUtil;
    private final VerificationService verificationService;
    private final NotificationService notificationService;
    private final PasswordEncoder encoder;

    @Transactional
    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto) {
        SignedUps user = signedUpsRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("user with this email doesn't exists"));
        String otp = otpGenerationUtil.generateOtp(6);
        verificationService.StoreEmailOtp(user.getEmail(),otp,900);
        notificationService.sendPasswordReset(new EmailOtpNotificationDto(user.getEmail(), otp));

        signedUpsRepository.save(user);
        return new ForgotPasswordResponseDto("OTP sent to registered email");
    }

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
        if (requestDto.getOldPassword().equals(requestDto.getNewPassword())) {
            throw new RuntimeException("old password and new password can not be same");
        }
        SignedUps user = signedUpsRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("user with this email doesn't exists"));

        if (!encoder.matches(requestDto.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedAccessException("Password doesn't matches");
        }
        user.setPassword(encoder.encode(requestDto.getNewPassword()));
        signedUpsRepository.save(user);
        return new ResetPasswordResponseDto("password reset successfully");
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto requestDto ){
        SignedUps user = signedUpsRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("user with this email doesn't exists"));
        if(!user.isEmailVerified()){
            throw new RuntimeException("email is not verified");
        }
        user.setPassword(encoder.encode(requestDto.getPassword()));
        signedUpsRepository.save(user);
    }
}
