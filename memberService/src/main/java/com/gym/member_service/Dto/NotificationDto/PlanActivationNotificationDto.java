package com.gym.member_service.Dto.NotificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PlanActivationNotificationDto {
    private String planName;
    private String subject;
    private LocalDate activationDate;
    private LocalDate planExpiration;
    private Integer duration;
}