package com.gym.member_service.Dto.NotificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PlanActivationNotificationDto {
    private String mailId;
    private String phone;
    private String planName;
    private String subject;
    private LocalDate activationDate;
    private LocalDate planExpiration;
    private Integer duration;
}
