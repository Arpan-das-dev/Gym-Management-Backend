package com.gym.authservice.Utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerationUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String DIGITS = "0123456789";

    public String generateOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than 0");
        }
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }

}
