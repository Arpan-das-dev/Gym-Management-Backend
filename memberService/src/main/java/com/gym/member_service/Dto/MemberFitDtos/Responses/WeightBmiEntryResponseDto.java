package com.gym.member_service.Dto.MemberProfieDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeightBmiEntryResponseDto {
    private LocalDate date;
    private Double weight;
    private Double bmi;
}
