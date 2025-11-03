package com.gym.authservice.Service;

import com.gym.authservice.Dto.Response.EmailOtpNotificationDto;
import com.gym.authservice.Dto.Response.PhoneOtpNotificationDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Utils.OtpGenerationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final SignedUpsRepository signedUpsRepository;
    private final OtpGenerationUtil generationUtil;
    private final WebClientService notificationService;

    public void StoreEmailOtp(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set("OTP:EMAIL:" + key, otp, ttlSeconds, TimeUnit.SECONDS);
    }

    public void StorePhoneOtp(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set("OTP:PHONE:" + key, otp, ttlSeconds, TimeUnit.SECONDS);
    }

    public void sendEmailOtp(String email,String name) {
        String otpEmail = generationUtil.generateOtp(6);
        StoreEmailOtp(email, otpEmail, 900);
        notificationService.sendEmailOtp(new EmailOtpNotificationDto(email, otpEmail,name));
    }

    public void sendPhoneOtp(String phone, String name) {
        String otpPhone = generationUtil.generateOtp(6);
        StorePhoneOtp(phone, otpPhone, 900);
        notificationService.sendPhoneOtp(new PhoneOtpNotificationDto(phone, otpPhone,name));
    }

    public Mono<Boolean> verifyEmail(String key, String otp) {
        return signedUpsRepository.findByEmail(key)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(user -> {
                    String value = redisTemplate.opsForValue().get("OTP:EMAIL:" + key);
                    if (otp != null && otp.equals(value)) {
                        deleteEmailOtp(key);
                        user.setEmailVerified(true);
                        return signedUpsRepository.save(user)
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }

    @Transactional
    public Mono<Boolean> verifyPhone(String key, String otp) {
        return signedUpsRepository.findByPhone(key)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(user -> {
                    if (!user.isEmailVerified()) {
                        return Mono.error(new RuntimeException("Please verify email before verifying email"));
                    }

                    String redisKey = "OTP:PHONE:" + key;
                    String value = redisTemplate.opsForValue().get(redisKey); // ⚠️ blocking

                    if (otp != null && otp.equals(value)) {
                        deletePhoneOtp(key);
                        user.setPhoneVerified(true);
                        return signedUpsRepository.save(user)
                                .thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }


    public void deleteEmailOtp(String key) {
        redisTemplate.delete("OTP:EMAIL:" + key);
    }

    public void deletePhoneOtp(String key) {
        redisTemplate.delete("OTP:PHONE:" + key);
    }
}