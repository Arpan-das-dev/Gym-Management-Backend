package com.gym.planService.Dtos.OrderDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyRevenueResponseDto {
    private Integer currentMonthRevenue;
    private Double changeInPercentage;
}
