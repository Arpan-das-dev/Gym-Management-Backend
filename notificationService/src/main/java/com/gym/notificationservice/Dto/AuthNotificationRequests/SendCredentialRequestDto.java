package com.gym.notificationservice.Dto.AuthNotificationRequests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendCredentialRequestDto {
    @Email(message = "enter a valid email")
    @NotBlank(message = "email is required")
    private String email;
    @NotBlank(message = "password is required")
    private String password;
    @NotBlank(message = "name is required")
    private String name;
}
