package com.gym.adminservice.Dto.PlanDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UpdateResponseDto {

    private String id;
    private String planName;
    private Double price;
    private Integer duration;
    private List< String> features;

}
