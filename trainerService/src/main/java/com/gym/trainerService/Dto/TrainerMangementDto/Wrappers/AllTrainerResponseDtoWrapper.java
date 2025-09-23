package com.gym.trainerService.Dto.TrainerMangementDto.Wrappers;

import com.gym.trainerService.Dto.TrainerMangementDto.Responses.AllTrainerResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for a list of all trainer responses.
 * <p>
 * Encapsulates a list of {@link AllTrainerResponseDto} objects to avoid
 * serialization/deserialization issues with caching systems such as Redis.
 * </p>
 *
 * @see AllTrainerResponseDto
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllTrainerResponseDtoWrapper {
    List<AllTrainerResponseDto> allTrainerResponseDtoWrappers;
}
