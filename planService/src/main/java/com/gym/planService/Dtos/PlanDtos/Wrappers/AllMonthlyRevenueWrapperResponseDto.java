package com.gym.planService.Dtos.PlanDtos.Wrappers;

import com.gym.planService.Dtos.PlanDtos.Responses.MonthlyReviewResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllMonthlyRevenueWrapper {
    private List<MonthlyReviewResponseDto> reviewResponseDtoList;
}
