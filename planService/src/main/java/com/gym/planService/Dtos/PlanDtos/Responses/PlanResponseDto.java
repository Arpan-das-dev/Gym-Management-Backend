package com.gym.planService.Dtos.PlanDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the response details of a subscription plan.
 * <p>
 * This class encapsulates the plan's unique identifier, name, price, duration, and features list.
 * It is typically used to convey plan information in API responses.
 * </p>
 *
 * <p>Example JSON representation:</p>
 * <pre>
 * {
 *   "planId": "BASIC_2026",
 *   "planName": "Basic Plan",
 *   "price": 2999.0,
 *   "duration": 365,
 *   "planFeatures": [
 *       "Access to core features",
 *       "Email support",
 *       "Monthly updates"
 *   ]
 * }
 * </pre>
 *
 * <p>This DTO supports builder pattern for easy instantiation and immutability best practices.</p>
 *
 * @author : Arpan Das
 * @version : 1.0
 * @since : 2025-10-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponseDto {

    /**
     * Unique identifier of the plan.
     * Example: "BASIC_2026"
     */
    private String planId;

    /**
     * Human-readable name of the plan.
     * Example: "Basic Plan"
     */
    private String planName;

    /**
     * Price of the plan in the applicable currency.
     * Represented as a Double value.
     */
    private Double price;

    /**
     * Duration of the plan in days.
     * Example: 365 for a yearly plan.
     */
    private Integer duration;

    /**
     * List of features included in the plan.
     * Each feature is represented as a String.
     */
    private List<String> planFeatures;
}
