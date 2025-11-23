package com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanNotificationRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String userMail;

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 50, message = "Plan name must be between 3 and 50 characters")
    private String planName;

    @NotNull(message = "Plan duration is required")
    @Min(value = 1, message = "Plan duration must be at least 1 month")
    @Max(value = 36, message = "Plan duration cannot exceed 36 months")
    private Integer planDuration;

    @NotNull(message = "Plan price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Plan price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private Double planPrice;
}
