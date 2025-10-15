package com.gym.trainerService.Dto.SessionDtos.Responses;

import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * DTO representing the response for each session retrieved from the system.
 * <p>
 * This model is commonly wrapped inside {@link AllSessionsWrapperDto}.
 * </p>
 * @author Arpan Das
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllSessionResponseDto {

    /** Unique identifier of the session. */
    private String sessionId;

    /** ID of the member linked to this session. */
    private String memberId;

    /** Descriptive name of the session. */
    private String sessionName;

    /** Session start timestamp. */
    private LocalDateTime sessionStartTime;

    /** Session end timestamp. */
    private LocalDateTime sessionEndTime;
}
