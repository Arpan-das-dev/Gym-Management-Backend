package com.gym.member_service.Dto.MemberTrainerRequestDto.Requests;
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
public class AddTrainerRequestDto {
    private String trainerId;
    private String trainerName;
    private String trainerProfileImageUrl;
    private LocalDate eligibilityEnd;
    private String memberId;
}
