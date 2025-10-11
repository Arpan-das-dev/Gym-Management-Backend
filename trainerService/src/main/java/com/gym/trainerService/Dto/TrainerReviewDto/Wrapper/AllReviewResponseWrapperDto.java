package com.gym.trainerService.Dto.TrainerReviewDto.Wrapper;

import com.gym.trainerService.Dto.TrainerReviewDto.Responses.ReviewResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllReviewResponseWrapperDto {
    List<ReviewResponseDto> reviewResponseDtoList;
}
