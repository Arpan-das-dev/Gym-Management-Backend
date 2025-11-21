package com.gym.member_service.Dto.MemberManagementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginStreakResponseDto {
    private Integer logInStreak;
    private Integer maxLogInStreak;
}
