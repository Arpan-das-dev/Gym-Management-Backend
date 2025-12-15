package com.gym.adminservice.Dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllReportsList {
    private String userId;
    private String userName;
    private String userRole;
    private String subject;
    private String message;
    private LocalDateTime messageTime;
    private String messageStatus;
}
