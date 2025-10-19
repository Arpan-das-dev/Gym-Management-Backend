package com.gym.planService.Dtos.CuponDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing the response details of a coupon code.
 * <p>
 * Used to transfer coupon code data such as the code string, its validity date,
 * and the discount percentage offered by the coupon.
 * </p>
 * <p>
 * This class follows Oracle’s recommended JavaDoc style for clarity and maintainability,
 * providing a standard structure for documenting DTO fields.
 * </p>
 *
 * <p>Example JSON representation:</p>
 * <pre>
 * {
 *   "cuponCode": "SPRING50",
 *   "validityDate": "2026-04-30",
 *   "offPercentage": 50.0
 * }
 * </pre>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 2025-10-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CuponCodeResponseDto {

    /**
     * The unique code string for the coupon.
     * Example: "SPRING50"
     */
    private String cuponCode;

    /**
     * The expiration date of the coupon code.
     * Coupons are valid through this date.
     */
    private LocalDate validityDate;

    /**
     * The discount percentage provided by the coupon.
     * Represented as a double value between 1.0 and 100.0 inclusive.
     */
    private Double offPercentage;
}
