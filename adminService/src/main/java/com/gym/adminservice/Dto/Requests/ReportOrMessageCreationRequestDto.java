package com.gym.adminservice.Dto.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportOrMessageCreationRequestDto {
    private String subject;
    private String userId;
    private String userRole;
    private String userName;
    private String emailId;
    private String message;
    private LocalDateTime messageTime;
}
