package com.gym.trainerService.Dto.TrainerMangementDto.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for adding or updating trainer specialities.
 * <p>
 * Contains a list of speciality names that can be added to a trainer.
 * Used in requests for adding specialities to a trainer profile.
 * </p>
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpecialityResponseDto {
    List<String> specialityList;
}
