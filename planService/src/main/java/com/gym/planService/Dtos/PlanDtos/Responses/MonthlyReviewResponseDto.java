package com.gym.planService.Dtos.PlanDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReviewResponseDto {
    private int year;
    private String month;
    private Double revenue;
    private Double change;
}
