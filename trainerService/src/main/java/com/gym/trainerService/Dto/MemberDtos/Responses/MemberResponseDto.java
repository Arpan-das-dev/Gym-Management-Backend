package com.gym.trainerService.Dto.MemberDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {
    private String memberId;
    private String trainerId;
    private String memberName;
    private String memberProfileImageUrl;
    private LocalDate eligibilityEnd;
}
