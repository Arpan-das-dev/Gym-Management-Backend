package com.gym.planService.Dtos.CuponDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCuponCodeRequestDto {

    private String cuponCode;
    private LocalDate validity;
    private Double offPercentage;
}
