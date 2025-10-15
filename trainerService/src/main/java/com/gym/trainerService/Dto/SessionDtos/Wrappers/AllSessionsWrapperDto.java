package com.gym.trainerService.Dto.SessionDtos.Wrappers;

import com.gym.trainerService.Dto.SessionDtos.Responses.AllSessionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO containing a list of session response objects.
 * <p>
 * Commonly used to send a paginated or grouped list of sessions
 * for a given trainer.
 * </p>
 * @author Arpan
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllSessionsWrapperDto {

    /** Collection of session details. */
    List<AllSessionResponseDto> responseDtoList;
}
