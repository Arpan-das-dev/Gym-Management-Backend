package com.gym.notificationservice.Dto.AuthNotificationRequests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageOrReportNotificationRequestDto {
    @Email(message = "sendTo must be a valid email address")
    @NotBlank(message = "sendTo cannot be blank")
    private String sendTo;

    @NotBlank(message = "subject cannot be blank")
    @Size(max = 255, message = "subject must be at most 255 characters")
    private String subject;

    @NotBlank(message = "message cannot be blank")
    private String message;
}
