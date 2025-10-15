package com.gym.trainerService.Dto.SessionDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * DTO representing the session information sent to another service
 * (for example, Member Service) after a new session is created.
 * @author Arpan Das
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponseDto {

    /** Unique identifier of the session. */
    private String sessionId;

    /** Name/title of the session. */
    private String sessionName;

    /** Date/time of the session. */
    private LocalDateTime sessionDate;

    /** Duration of the session in hours. */
    private Double duration;
}
