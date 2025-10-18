package com.gym.planService.Dtos.PlanDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponseDto {

    private String planId;
    private String planName;
    private Double price;
    private Integer duration;
    private List<String> planFeatures;
}
