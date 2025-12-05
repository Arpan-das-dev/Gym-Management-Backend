package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerDashBoardInfoResponseDto {
    private int currentClientCount;
    private int lastMonthClientCount;
    private int totalSessionsThisWeek;
    private int totalSessionsLeft;
    private double currentRating;
    private double LastMonthRating;
}
