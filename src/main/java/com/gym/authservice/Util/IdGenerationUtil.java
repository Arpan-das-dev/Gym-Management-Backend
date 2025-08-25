package com.gym.authservice.Util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class IdGenerationUtil {
    public String idGeneration(String role, String gender, LocalDate joinedDate){
        String year = String.valueOf(joinedDate.getYear()).substring(2);
        String Date = String.valueOf(joinedDate.getDayOfMonth());
        char Month =(char)('a'+joinedDate.getMonthValue()-1);

        LocalDateTime dateTime = LocalDateTime.now();
        char hours = (char)('A'+dateTime.getHour()-1);
        int mins = dateTime.getMinute();
        char sec = (char)('a'+dateTime.getSecond()/3);

        Random random = new Random();
        int value = 10+random.nextInt(26);

        String  id = "FS"+role.substring(0,1)+"-"+year+gender.substring(0,1)
                +Date+Month+Date+hours+mins+value+sec;
        return id;
    }
}
