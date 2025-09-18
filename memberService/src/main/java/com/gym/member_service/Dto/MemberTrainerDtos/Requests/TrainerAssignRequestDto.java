package com.gym.member_service.Dto.MemberTrainerDtos.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainerAssignRequestDto {
    @NotBlank(message = "Member ID must not be blank")
    private String memberId;

    @NotBlank(message = "Trainer ID must not be blank")
    private String trainerId;

    @NotBlank(message = "TrainerName can not be empty")
    private String trainerName;

    @NotBlank(message = "Trainer profile image URL must be a valid URL")
    private String trainerProfileImageUrl;

    @NotNull(message = "Request date must not be null")
    @PastOrPresent(message = "Request date cannot be in the future")
    private LocalDate requestDate;
}
