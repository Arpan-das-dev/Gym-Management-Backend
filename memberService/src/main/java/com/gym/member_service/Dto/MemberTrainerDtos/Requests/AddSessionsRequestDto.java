package com.gym.member_service.Dto.MemberTrainerRequestDto.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddSessionsRequestDto {

    @NotBlank(message = "Session name must not be blank")
    private String sessionName;

    @NotNull(message = "Session date must not be null")
    @FutureOrPresent(message = "Session date must be in the present or future")
    private LocalDateTime sessionDate;

    @Positive(message = "duration can not be in negative")
    private double duration;
}
