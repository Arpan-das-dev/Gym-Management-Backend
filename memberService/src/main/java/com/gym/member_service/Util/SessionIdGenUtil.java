package com.gym.member_service.Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class SessionIdGenUtil {
    public String generateSessionId(String memberId, String trainerId, LocalDateTime startDate, LocalDateTime endDate) {
        String input = memberId + ":" + trainerId + ":" + startDate.toString() + ":" + endDate.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, 16);
        } catch (
                NoSuchAlgorithmException e) {
            // Fallback in the very unlikely case SHA-256 is unavailable
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16); }
        }
}
