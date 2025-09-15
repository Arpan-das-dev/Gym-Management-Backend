package com.gym.member_service.Dto.MemberFitDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class BmiSummaryResponseDto {
    private double avgBmi;
    private double minBmi;
    private double maxBmi;
    private double avgWeight;
    private double minWeight;
    private double maxWeight;
    private int entryCount;
}
