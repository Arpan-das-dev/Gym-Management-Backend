package com.gym.adminservice.Dto.Requests;

import java.time.LocalDate;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainerAssignMentRequestDto {

    @NotBlank(message = "memberId is required")
    private String memberId;
    
    @NotBlank(message = "Trainer ID must not be blank")
    private String trainerId;

    @NotBlank(message = "Trainer name must not be blank")
    private String trainerName;

    @URL(message = "Trainer profile image URL must be a valid URL")
    private String trainerProfileImageUrl;

    @NotNull(message = "Eligibility end date must not be null")
    @FutureOrPresent(message = "Eligibility end date must be present or in the future")
    private LocalDate eligibilityEnd;
}
