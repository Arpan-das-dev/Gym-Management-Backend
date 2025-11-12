package com.gym.planService.Dtos.CuponDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing a request to update an existing coupon configuration.
 *
 * <p>This class is typically used by administrative or back-office operations to
 * update coupon details such as associated plan, validity, and discount percentage.</p>
 *
 * <p>Example JSON request:</p>
 * <pre>
 * {
 *   "planId": "PLAN2025_BASIC",
 *   "validity": "2026-03-31",
 *   "offPercentage": 15.0
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
public class UpdateCuponRequestDto {

    /**
     * Identifier of the plan or offer linked with this coupon.
     * <p>Must be alphanumeric and between 4–30 characters in length.</p>
     */
    @NotBlank(message = "Plan ID cannot be empty.")
    @Size(min = 4, max = 30, message = "Plan ID must be between 4 and 30 characters long.")
    @Pattern(regexp = "^[A-Z0-9_\\-]+$", message = "Plan ID must contain only uppercase letters, numbers, underscores, or hyphens.")
    private String planId;

    @NotNull(message = "Coupon validity From is required.")
    @Future(message = "Coupon validity date must be a future date.")
    private LocalDate validFrom;
    /**
     * The new expiration date for the coupon.
     * <p>Must be a valid future date to remain active for users.</p>
     */
    @NotNull(message = "Coupon validity date is required.")
    @Future(message = "Coupon validity date must be a future date.")
    private LocalDate validity;

    /**
     * Updated discount percentage for the coupon.
     * <p>Valid range: 1% to 100%.</p>
     */
    @NotNull(message = "Discount percentage is required.")
    @DecimalMin(value = "1.0", message = "Discount must be at least 1%.")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%.")
    private Double offPercentage;

    @NotNull(message = "Can not create a cupon code without setting it's access")
    @NotBlank(message = "Can not create a cupon code without setting it's access eg: Private, Public")
    private String access;

    private String description;
}
