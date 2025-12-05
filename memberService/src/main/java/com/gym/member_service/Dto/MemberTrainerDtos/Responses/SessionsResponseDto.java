package com.gym.member_service.Dto.MemberTrainerDtos.Responses;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
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
    private String sessionStatus;
}
