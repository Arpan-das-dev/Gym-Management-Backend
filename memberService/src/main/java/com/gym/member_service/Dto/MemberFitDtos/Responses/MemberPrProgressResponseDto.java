package com.gym.member_service.Dto.MemberFitDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberPrProgressResponseDto {
    private String workoutName;
    private Double weight;
    private Integer repetitions;
    private LocalDate achievedDate;
}
