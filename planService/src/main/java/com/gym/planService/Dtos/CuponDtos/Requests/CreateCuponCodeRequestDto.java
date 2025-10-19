package com.gym.planService.Dtos.CuponDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing a request to create a new coupon code in the system.
 *
 * <p>This class is typically used as part of a REST API request body when creating promotional
 * coupons. It ensures that all required fields are provided and valid according to
 * production-level data integrity rules.</p>
 *
 * <p>Example JSON request:</p>
 * <pre>
 * {
 *   "cuponCode": "WELCOME10",
 *   "validity": "2025-12-31",
 *   "offPercentage": 10.0
 * }
 * </pre>
 *
 * @author Arpan Das
 * @since 2025-10-19
 * @version 1.0
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCuponCodeRequestDto {

    /**
     * Unique coupon code string that users can apply to get discounts.
     * <p>Must contain 4–20 characters, uppercase letters, digits, or underscores only.</p>
     */
    @NotBlank(message = "Coupon code cannot be empty.")
    @Size(min = 4, max = 20, message = "Coupon code must be between 4 and 20 characters long.")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Coupon code must contain only uppercase letters, digits, or underscores.")
    private String cuponCode;

    /**
     * Expiration date of the coupon code.
     * <p>Must be a future date to ensure the coupon is still valid when created.</p>
     */
    @NotNull(message = "Coupon validity date is required.")
    @Future(message = "Coupon validity date must be a future date.")
    private LocalDate validity;

    /**
     * Discount percentage provided by the coupon.
     * <p>Must be a positive number between 1 and 100.</p>
     */
    @NotNull(message = "Discount percentage is required.")
    @DecimalMin(value = "1.0", message = "Discount must be at least 1%.")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%.")
    private Double offPercentage;
}
