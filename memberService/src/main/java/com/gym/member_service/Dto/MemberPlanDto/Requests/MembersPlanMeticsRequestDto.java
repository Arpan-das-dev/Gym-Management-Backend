package com.gym.member_service.Dto.MemberManagementDto.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembersPlanMeticsRequestDto {
    private List<String> planNames;
}
