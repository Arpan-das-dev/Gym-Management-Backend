package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientMatrixInfo {
    private int currentMonthClientCount;
    private int previousMonthClientCount;
    private double change;
}
