package com.gym.adminservice.Dto.Responses;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class MemberAssignmentToTrainerResponseDto {
    private String memberId;
    private String trainerId;
    private String memberName;
    private String memberProfileImageUrl;
    private LocalDate eligibilityEnd;
}
