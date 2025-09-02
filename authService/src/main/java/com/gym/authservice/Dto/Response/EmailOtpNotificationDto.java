package com.gym.authservice.Dto.Response;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailOtpNotificationDto {
    private String email;
    private String otp;
}
