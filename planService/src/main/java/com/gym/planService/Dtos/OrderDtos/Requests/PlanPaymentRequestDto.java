package com.gym.planService.Dtos.OrderDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanPaymentRequestDto {

    @NotBlank(message = "User name is required")
    private String userName;

    @NotBlank(message = "User ID is required")
    private String userId;

    @Email(message = "User email must be a valid email address")
    @NotBlank(message = "User email is required")
    private String userMail;

    @NotBlank(message = "Plan ID is required")
    private String planId;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    private Double amount;

    // Optional coupon code, can be null or empty
    private String cuponCode;

    @NotNull(message = "Payment date is required")
    @FutureOrPresent(message = "Payment date cannot be in the past")
    private LocalDateTime paymentDate;
}
