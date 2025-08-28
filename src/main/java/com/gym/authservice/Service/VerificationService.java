package com.gym.authservice.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final StringRedisTemplate redisTemplate;

    public void StoreEmailOtp(String key,String otp,long ttlSeconds){
        redisTemplate.opsForValue().set("OTP:EMAIL:"+key,otp,ttlSeconds, TimeUnit.SECONDS);
    }
    public void StorePhoneOtp(String key,String otp,long ttlSeconds){
        redisTemplate.opsForValue().set("OTP:PHONE"+key,otp,ttlSeconds,TimeUnit.SECONDS);
    }

    public boolean verifyEmail(String key,String otp){
        String value = redisTemplate.opsForValue().get("OTP:EMAIL"+key);
        return otp !=null && otp.equals(value);
    }
    public boolean verifyPhone(String key,String otp){
        String value = redisTemplate.opsForValue().get("OTP:PHONE"+key);
        return otp != null && otp.equals(value);
    }

    public void deleteEmailOtp(String key){
        redisTemplate.delete("OTP:EMAIL"+key);
    }
    public void deletePhoneOtp(String key){
        redisTemplate.delete("OTP:PHONE"+key);
    }
}
