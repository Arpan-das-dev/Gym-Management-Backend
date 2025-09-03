package com.gym.notificationservice.Dto.AuthNotificationRequests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WelcomeRequestDto {
    @NotBlank(message = "Id required")
    private String id;
    @Email(message = "Enter a valid Email")
    private String email;
    @NotBlank(message = "phone no is required")
    @Min(value = 10, message = "phone number must have 10 digits")
    @Max(value = 12, message = "phone number can not be more than 12 digits")
    private String phone;
    @NotBlank(message = "Name is required")
    private String name;
}
