package com.gym.trainerService.Dto.MemberDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionMatrixInfo {
    private int totalSessionsThisWeek;
    private int totalSessionsLeft;
}
