package com.gym.member_service.Dto.MemberFitDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyRoutineResponseDto {
    private LocalDate routineDate;
    private String day;
    private List<ExerciseResponseDto> exercises;
}
