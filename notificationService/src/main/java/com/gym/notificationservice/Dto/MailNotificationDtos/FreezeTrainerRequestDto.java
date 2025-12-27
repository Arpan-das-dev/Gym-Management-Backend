package com.gym.notificationservice.Dto.MailNotificationDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreezeTrainerRequestDto {
    private String trainerName;
    private String trainerMail;
    private String subject;
    private boolean frozen;
    private String time;
}
