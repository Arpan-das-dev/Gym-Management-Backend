package com.gym.adminservice.Utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ReportIdGenUtil {
    public String createMessageId(String userId, LocalDateTime messageTime){
        return userId+messageTime.toString()+ ThreadLocalRandom.current().toString();
    }
}
