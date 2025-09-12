package com.gym.member_service.Dto.MemberManagementDto.Responses;

import com.gym.member_service.Model.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class MemberPlansMeticsResponseDto {
    private String planName;
    private int membersCount;
    private List<Member> memberList;
}
