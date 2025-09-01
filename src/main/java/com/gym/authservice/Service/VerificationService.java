package com.gym.authservice.Service;

import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final SignedUpsRepository signedUpsRepository;

    public void StoreEmailOtp(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set("OTP:EMAIL:" + key, otp, ttlSeconds, TimeUnit.SECONDS);
    }

    public void StorePhoneOtp(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set("OTP:PHONE:" + key, otp, ttlSeconds, TimeUnit.SECONDS);
    }


    @Transactional
    public boolean verifyEmail(String key, String otp) {
        SignedUps user = signedUpsRepository.findByEmail(key)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

            String value = redisTemplate.opsForValue().get("OTP:EMAIL:" + key);
            if (otp != null && otp.equals(value)) {
                deleteEmailOtp(key);
                user.setEmailVerified(true);
                return true;
            }

        return false;
    }

    @Transactional
    public boolean verifyPhone(String key, String otp) {
        SignedUps user = signedUpsRepository.findByPhone(key)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify email before verifying phone");
        }

        String value = redisTemplate.opsForValue().get("OTP:PHONE:" + key);
        if (otp != null && otp.equals(value)) {
            deletePhoneOtp(key);
            user.setPhoneVerified(true);
            return true;
        }
        return false;
    }

    public void deleteEmailOtp(String key) {
        redisTemplate.delete("OTP:EMAIL:" + key);
    }

    public void deletePhoneOtp(String key) {
        redisTemplate.delete("OTP:PHONE:" + key);
    }
}