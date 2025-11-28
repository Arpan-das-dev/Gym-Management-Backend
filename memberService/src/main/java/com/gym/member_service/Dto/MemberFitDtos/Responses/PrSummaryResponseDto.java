package com.gym.member_service.Dto.MemberFitDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrSummaryResponseDto {
    private String workoutName;
    private double avgWeight;
    private int avgReps;
    private double maxWeight;
    private int maxReps;
    private int entryCount;
    private int monthValue;
    private int year;
}
