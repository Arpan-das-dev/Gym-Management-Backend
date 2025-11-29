package com.gym.member_service.Dto.MemberFitDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BmiWeightInfoResponseDto {
    private double currentBmi;
    private double changedBmiFromLastMonth;
    private double currentBodyWeight;
    private double changedBodyWeightFromLastMonth;
    private LocalDate latestDate;
    private LocalDate oldDateTime;
}
