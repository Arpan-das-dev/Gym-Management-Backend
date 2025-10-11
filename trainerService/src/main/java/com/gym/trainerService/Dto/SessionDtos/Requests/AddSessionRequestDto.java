package com.gym.trainerService.Dto.SessionDtos.Requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddSessionRequestDto {

    @NotBlank(message = "MemberId is required")
    private String  memberId;

    @NotBlank(message = "Session name must not be blank")
    private String sessionName;

    @NotNull(message = "Session date must not be null")
    @FutureOrPresent(message = "Session date must be in the present or future")
    private LocalDateTime sessionDate;

    @Positive(message = "duration can not be in negative")
    private double duration;
}
