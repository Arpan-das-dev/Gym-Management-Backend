package com.gym.member_service.Dto.MemberTrainerDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSessionRequestDto {
    private String trainerId;
    private String sessionName;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
}
