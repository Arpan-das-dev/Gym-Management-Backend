package com.gym.notificationservice.Dto.AuthNotificationRequests;

import jakarta.validation.constraints.*;
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
    @Size(min = 10, max = 12, message = "phone number must be between 10 and 12 digits")
    private String phone;
    @NotBlank(message = "Name is required")
    private String name;
}
