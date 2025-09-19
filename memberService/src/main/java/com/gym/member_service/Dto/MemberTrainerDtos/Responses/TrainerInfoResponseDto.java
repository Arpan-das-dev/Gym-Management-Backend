package com.gym.member_service.Dto.MemberTrainerDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TrainerInfoResponseDto {

    private String trainerId;
    private String trainerName;
    private String profileImageUrl;
    private LocalDate eligibilityDate;
}
