package com.gym.trainerService.Dto.SessionDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing the response after updating an existing session.
 * <p>
 * Used to confirm the updated session details returned to the client.
 * </p>
 * @author Arpan Das
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSessionResponseDto {

    /** ID of the trainer associated with the session. */
    private String trainerId;

    /** Name/title of the updated session. */
    private String sessionName;

    /** Updated session start time. */
    private LocalDateTime sessionStartTime;

    /** Updated session end time. */
    private LocalDateTime sessionEndTime;
}
