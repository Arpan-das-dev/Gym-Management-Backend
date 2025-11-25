package com.gym.member_service.Dto.MemberPlanDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberPlanInfoResponseDto {
    private LocalDateTime planExpiration;
    private String planId;
    private String planName;
    private Integer planDurationLeft;
}
