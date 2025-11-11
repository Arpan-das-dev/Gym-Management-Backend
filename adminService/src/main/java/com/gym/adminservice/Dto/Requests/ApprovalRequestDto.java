package com.gym.adminservice.Dto.Requests;

import com.gym.adminservice.Enums.RoleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequestDto {
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    @Size(min = 10, max = 12, message = "Phone number must be between 10 to 12 digits")
    private String phone;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Role is required")
    private RoleType role;

    @NotNull(message = "Join date is required")
    @PastOrPresent(message = "Join date cannot be in the future")
    private LocalDate joinDate;
}
