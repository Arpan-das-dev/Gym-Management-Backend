package com.gym.member_service.Dto.NotificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailNotificationDto {
    private String memberId;
    private String name;
    private String mailId;
    private String phone;
    private String subject;
    private LocalDateTime time;
}
