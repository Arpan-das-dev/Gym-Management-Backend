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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanUpdateRequestDto {

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
