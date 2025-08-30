package com.gym.authservice.Utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class IdGenerationUtil {
    public String idGeneration(String role, String gender, LocalDate joinedDate){
        String year = String.valueOf(joinedDate.getYear()).substring(2);
        String day = String.format("%02d", joinedDate.getDayOfMonth());
        char month = (char) ('A' + joinedDate.getMonthValue() - 1);

        LocalDateTime now = LocalDateTime.now();
        String hour = String.format("%02d", now.getHour());
        String minute = String.format("%02d", now.getMinute());
        String milli = String.format("%03d", System.currentTimeMillis() % 1000);

        int randomValue = 100 + ThreadLocalRandom.current().nextInt(900);

        String roleChar = (role != null && !role.isEmpty()) ? role.substring(0, 1).toUpperCase() : "X";
        String genderChar = (gender != null && !gender.isEmpty()) ? gender.substring(0, 1).toUpperCase() : "U";

        return String.format("FS%s-%s%s%s%s%s%s%s",
                roleChar, year, genderChar, day, month, hour, minute, randomValue + milli);
    }
}
