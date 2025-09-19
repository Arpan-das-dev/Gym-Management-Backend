package com.gym.member_service.Dto.MemberTrainerDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionsResponseDto {
    private String sessionId;
    private String sessionName;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private String memberId;
    private String trainerId;
}
