package com.gym.trainerService.Dto.TrainerReviewDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingMatrixInfo {
    private double currentRating;
    private double oldRating;
    private double change;
}
