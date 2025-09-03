package com.gym.notificationservice.Dto.AuthNotificationRequests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneOtpRequestDto {
    @NotBlank(message = "phone no is required")
    @Size(min = 10,max =12,message = "phone no must be in 10 to 12 digits")
    private String phone;
    @NotBlank(message = "otp is required")
    @Size(min = 6,max = 6,message = "otp should be in six digit")
    private String otp;
}
