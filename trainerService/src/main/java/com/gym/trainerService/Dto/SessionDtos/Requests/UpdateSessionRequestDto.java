package com.gym.trainerService.Dto.SessionDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSessionRequestDto {

    @NotBlank(message = "Trainer id is required")
    private String trainerId;

    @NotBlank(message = "Member id is required")
    private String memberId;

    @NotBlank(message = "Session name is required")
    @Size(min = 4, max = 30, message = "Session name can contains only 4-30 numbers of characters")
    private String sessionName;

    @NotNull(message = "Session date must not be null")
    @FutureOrPresent(message = "Session date must be in the present or future")
    private LocalDateTime sessionDate;

    @Positive(message = "duration can not be in negative")
    private double duration;
}
