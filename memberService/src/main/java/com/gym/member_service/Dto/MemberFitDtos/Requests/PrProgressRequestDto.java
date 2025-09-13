package com.gym.member_service.Dto.MemberFitDtos.Requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PrProgressRequestDto {

    @NotNull(message = "Name of workout is required")
    @NotBlank(message = "Name of workout cannot be blank")
    @Size(min = 4, max = 40, message = "Workout name should be between 4 and 40 characters")
    private String workoutName;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @NotNull(message = "Repetitions is required")
    @Positive(message = "Repetitions must be positive")
    private Integer repetitions;

    @NotNull(message = "Achieved date is required")
    @PastOrPresent(message = "Achieved date cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate achievedDate;
}
