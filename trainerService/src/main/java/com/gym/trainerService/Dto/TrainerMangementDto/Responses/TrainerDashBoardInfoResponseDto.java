package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import com.gym.trainerService.Dto.MemberDtos.Responses.SessionMatrixInfo;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.RatingMatrixInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerDashBoardInfoResponseDto {
    private ClientMatrixInfo clientMatrixInfo;
    private SessionMatrixInfo sessionMatrixInfo;
    private RatingMatrixInfo ratingMatrixInfo;
}
