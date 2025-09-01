package com.gym.authservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhoneOtpNotificationDto {
    private String phone;
    private String otp;
}
