package com.gym.trainerService.Dto.TrainerMangementDto.Wrappers;

import com.gym.trainerService.Dto.TrainerMangementDto.Responses.PublicTrainerInfoResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllPublicTrainerInfoResponseWrapperDto {
    List<PublicTrainerInfoResponseDto> publicTrainerInfoResponseDtoList;
}
