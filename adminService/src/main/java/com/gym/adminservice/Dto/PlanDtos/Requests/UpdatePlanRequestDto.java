package com.gym.adminservice.Dto.PlanDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class UpdatePlanRequestDto {
    @NotBlank(message = "id is required to update plan")
    private String  id;
    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1")
    private Integer duration;

    @NotNull(message = "Features list cannot be null")
    @Size(min = 1, message = "There must be at least one feature")
    private List<@NotBlank(message = "Feature cannot be blank") String> features;
}
