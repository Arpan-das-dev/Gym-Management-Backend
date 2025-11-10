package com.gym.planService.Dtos.CuponDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CuponValidationResponseDto {
    private boolean valid;
    private double offPercentage;
}
