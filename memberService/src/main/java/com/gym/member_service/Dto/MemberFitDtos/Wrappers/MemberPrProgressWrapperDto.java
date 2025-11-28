package com.gym.member_service.Dto.MemberFitDtos.Wrappers;

import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberPrProgressWrapperDto {
    private List<MemberPrProgressResponseDto> responseDtoList;

    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;
}

