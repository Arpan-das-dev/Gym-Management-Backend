package com.gym.planService.Utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class PaymentIdGenUtil {
    public String generatePaymentId(String userId, String planId, LocalDateTime paymentDate) {
        // Format the date: YYYY/MM/DD/HH/MM/SS
        String timestamp = paymentDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Extract last 4 characters of userId and planId for traceability
        String userPart = userId.length() > 4 ? userId.substring(userId.length() - 4).toUpperCase() : userId.toUpperCase();
        String planPart = planId.length() > 3 ? planId.substring(0, 3).toUpperCase() : planId.toUpperCase();

        // Add a small random alphanumeric suffix for collision safety
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Build the final ID
        return String.format("FS-PAY-%s-U%s-P%s-%s", timestamp, userPart, planPart, randomSuffix);
    }
}
