package com.gym.planService.Dtos.CuponDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CuponCodeResponseDto {
    private String cuponCode;
    private LocalDate validityDate;
    private Double offPercentage;
}
