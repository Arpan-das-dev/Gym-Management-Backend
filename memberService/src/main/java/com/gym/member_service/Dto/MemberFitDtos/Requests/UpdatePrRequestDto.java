package com.gym.member_service.Dto.MemberFitDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePrRequestDto {
    /**
     * Working weight for this PR attempt in kilograms.
     * Must be positive and realistically capped to avoid bad input.
     */
    @NotNull(message = "Weight is required.")
    @DecimalMin(value = "1.0", message = "Weight must be at least {value} kg.")
    @Max(value = 500, message = "Weight must not exceed {value} kg.")
    private Double weight;

    /**
     * Repetition count for the PR attempt.
     * Typical strength ranges are 1–30 reps.
     */
    @NotNull(message = "Repetitions are required.")
    @Min(value = 1, message = "Repetitions must be at least {value}.")
    @Max(value = 20, message = "Repetitions must not exceed {value}.")
    private Integer repetitions;

    /**
     * Date when this PR was achieved.
     * Must be in the past or today; future dates are rejected.
     */
    @NotNull(message = "Archived date is required.")
    @PastOrPresent(message = "Archived date cannot be in the future.")
    private LocalDate archivedDate;

}
