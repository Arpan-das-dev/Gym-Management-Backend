package com.gym.trainerService.Dto.SessionDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a request to update an existing training session.
 * <p>
 * This DTO is used when a trainer modifies session details such as date,
 * name, or duration.
 * </p>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Trainer and Member IDs must not be blank.</li>
 *   <li>Session name length between 4–30 characters.</li>
 *   <li>Session date cannot be in the past.</li>
 * </ul> *
 * @author Arpan Das
 * @since 1.0
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSessionRequestDto {

    /** Identifier of the trainer responsible for the session. */
    @NotBlank(message = "Trainer id is required")
    private String trainerId;

    /** Identifier of the member attending the session. */
    @NotBlank(message = "Member id is required")
    private String memberId;

    /** Updated name/title of the session. */
    @NotBlank(message = "Session name is required")
    @Size(min = 4, max = 30, message = "Session name must contain 4–30 characters")
    private String sessionName;

    /** Updated session date/time (must be present or future). */
    @NotNull(message = "Session date must not be null")
    private LocalDateTime sessionDate;

    /** Updated duration (in hours). */
    @Positive(message = "Duration cannot be negative")
    @DecimalMin(value = "0.5", message = "Minimum duration is 0.5 hours (30 mins)")
    @DecimalMax(value = "2.5", message = "Maximum duration is 2.5 hours (2 hrs 30 mins)")
    private double duration;

}
