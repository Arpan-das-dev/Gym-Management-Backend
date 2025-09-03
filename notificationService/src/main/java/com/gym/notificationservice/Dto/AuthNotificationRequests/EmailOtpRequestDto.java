package com.gym.notificationservice.Dto.AuthNotificationRequests;

import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailOtpRequestDto {
    @Email(message = "Enter a valid Email")
    private String email;
    @NotBlank(message = "Otp is required")
    @Size(message = "otp must have 6 digits or characters")
    private String otp;

}
