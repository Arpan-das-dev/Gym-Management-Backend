package com.gym.trainerService.Dto.SessionDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a request to create a new training session.
 * <p>
 * Contains details such as member ID, session name, date/time, and duration.
 * Validation annotations ensure correct data before reaching the service layer.
 * </p>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>{@code @NotBlank} → Ensures required string fields are not empty.</li>
 *   <li>{@code @FutureOrPresent} → Session date must not be in the past.</li>
 *   <li>{@code @DecimalMin}/{@code @DecimalMax} → Restricts duration range.</li>
 * </ul>
 *
 * @author Arpan Das
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddSessionRequestDto {

    /** Unique identifier of the member linked to this session. */
    @NotBlank(message = "MemberId is required")
    private String memberId;

    /** Descriptive name of the session. */
    @NotBlank(message = "Session name must not be blank")
    private String sessionName;

    /** Date and time when the session will start. Must be current or future. */
    @NotNull(message = "Session date must not be null")
    @FutureOrPresent(message = "Session date must be in the present or future")
    private LocalDateTime sessionDate;

    /** Duration in hours. Range: 0.5 (30 mins) – 2.5 (2 hrs 30 mins). */
    @Positive(message = "Duration cannot be negative")
    @DecimalMin(value = "0.5", message = "Minimum duration is 0.5 hours (30 mins)")
    @DecimalMax(value = "2.5", message = "Maximum duration is 2.5 hours (2 hrs 30 mins)")
    private double duration;
}
