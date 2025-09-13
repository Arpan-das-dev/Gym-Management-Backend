package com.gym.member_service.Dto.MemberFitDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberWeighBmiEntryRequestDto {

    @NotNull(message = "date is required")
    @PastOrPresent(message = "date cannot be in the future")
    private LocalDate date;

    @NotNull(message = "weight is required")
    @Positive(message = "weight must be positive")
    private Double weight;

    @NotNull(message = "bmi is required")
    @DecimalMin(value = "10.0", message = "bmi must be at least 10")
    @DecimalMax(value = "50.0", message = "bmi must be at most 50")
    private Double bmi;
}
