package com.gym.planService.Dtos.PlanDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuickStatsResponseDto {
    private Double yearlyIncome;
    private Double monthlyIncome;
    private Double todayIncome;
}
