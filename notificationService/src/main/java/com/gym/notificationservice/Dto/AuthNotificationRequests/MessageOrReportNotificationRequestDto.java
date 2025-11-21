package com.gym.notificationservice.Dto.AuthNotificationRequests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageOrReportNotificationRequestDto {
    private String sendTo;
    private String subject;
    private String message;
}
