package com.gym.member_service.Dto.MemberManagementDto.Wrappers;

import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberListResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllMembersInfoWrapperResponseDtoList {
    List<AllMemberListResponseDto> responseDtoList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    private boolean lastPage;
}
