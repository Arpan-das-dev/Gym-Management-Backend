package com.gym.member_service.Dto.MemberFitDtos.Wrappers;

import com.gym.member_service.Dto.MemberFitDtos.Responses.PrSummaryResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrSummaryResponseWrapperDto {
    List<PrSummaryResponseDto> responseDtoList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;

}
