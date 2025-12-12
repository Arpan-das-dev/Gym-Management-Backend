package com.gym.adminservice.Dto.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Subject is required.")
    @Size(max = 150, message = "Subject must not exceed 150 characters.")
    private String subject;

    @NotBlank(message = "User ID is required.")
    @Size(max = 64, message = "User ID must not exceed 64 characters.")
    private String userId;

    @NotBlank(message = "User role is required.")
    @Size(max = 50, message = "User role must not exceed 50 characters.")
    private String userRole;

    @NotBlank(message = "User name is required.")
    @Size(max = 100, message = "User name must not exceed 100 characters.")
    private String userName;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Email address format is invalid.")
    @Size(max = 255, message = "Email address must not exceed 255 characters.")
    private String emailId;

    @NotBlank(message = "Message content is required.")
    @Size(max = 2000, message = "Message must not exceed 2000 characters.")
    private String message;

    @NotNull(message = "Message time is required.")
    private LocalDateTime messageTime;
}

