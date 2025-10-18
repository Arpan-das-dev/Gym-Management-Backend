package com.gym.planService.Dtos.PlanDtos.Wrappers;

import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllPlanResponseWrapperDto {
    List<PlanResponseDto> responseDtoList;
}
