package com.gym.adminservice.Dto.PlanDtos.Responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class CreationResponseDto {

    private String planId;
    private String planName;
    private Double price;
    private Integer duration;
    private List< String> features;
}
