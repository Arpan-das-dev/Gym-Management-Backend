package com.gym.planService.Dtos.PlanDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanLifeTimeIncome {
    private Double revenue;
    private Long usage;
    private String planName;
}
