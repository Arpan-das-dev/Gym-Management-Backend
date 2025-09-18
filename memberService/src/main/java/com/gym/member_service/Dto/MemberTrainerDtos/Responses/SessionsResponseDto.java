package com.gym.member_service.Dto.MemberTrainerRequestDto.Responses;

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
    private String sessionName;
    private LocalDateTime sessionDate;
    private double sessionDuration;
    private String sessionNotes;
}
