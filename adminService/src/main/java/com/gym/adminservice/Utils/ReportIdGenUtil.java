package com.gym.adminservice.Utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ReportIdGenUtil {
    public String createMessageId(String userId, LocalDateTime time){
        return "MSG-" +
                time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "-" +
                userId.substring(0, Math.min(6, userId.length())) +
                "-" +
                ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
