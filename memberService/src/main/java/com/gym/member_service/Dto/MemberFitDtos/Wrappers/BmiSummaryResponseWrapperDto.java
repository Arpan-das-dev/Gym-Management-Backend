package com.gym.member_service.Dto.MemberFitDtos.Wrappers;

import com.gym.member_service.Dto.MemberFitDtos.Responses.BmiSummaryResponseDto;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BmiSummaryResponseWrapperDto {
   private List<BmiSummaryResponseDto> summaryResponseDto;
}
