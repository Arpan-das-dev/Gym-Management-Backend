package com.gym.planService.Dtos.PlanDtos.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for creating a new Plan.
 * <p>
 * This DTO carries the necessary fields required to create a plan including
 * a unique identifier, name, pricing, duration, and feature list.
 * It includes validation constraints to ensure input integrity.
 * </p>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *     <li><code>planId</code> must not be blank.</li>
 *     <li><code>planName</code> must not be blank.</li>
 *     <li><code>price</code> must be a positive Double &gt; 0.</li>
 *     <li><code>duration</code> must be a positive Integer &gt; 0.</li>
 *     <li><code>features</code> must not be null or empty.</li>
 * </ul>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 2025-10-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanCreateRequestDto {

    // Unique identifier for the plan, must not be blank.
    @NotBlank(message = "Plan ID is required and cannot be blank.")
    private String planId;

    // Name of the plan, must be provided and non-empty.
    @NotBlank(message = "Plan name must not be empty or blank.")
    private String planName;

    // Price of the plan in currency units, must be > 0.
    @NotNull(message = "Plan price is mandatory and must be provided.")
    @Positive(message = "Plan price must be greater than zero.")
    private Double price;

    // Duration of the plan in integer units (e.g. months), must be > 0.
    @NotNull(message = "Duration is mandatory and must be provided.")
    @Positive(message = "Duration must be a positive integer greater than zero.")
    private Integer duration;

    // List of feature descriptions included in the plan, must contain at least one non-blank entry.
    @NotNull(message = "Plan features list cannot be null.")
    @NotEmpty(message = "Plan features list must contain at least one feature.")
    private List<@NotBlank(message = "Feature descriptions must not be blank.") String> features;

}
