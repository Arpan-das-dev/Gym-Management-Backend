package com.gym.trainerService.Dto.SessionDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponseDto {
    private String sessionId;
    private String sessionName;
    private LocalDateTime sessionDate;
    private Double duration;
}
