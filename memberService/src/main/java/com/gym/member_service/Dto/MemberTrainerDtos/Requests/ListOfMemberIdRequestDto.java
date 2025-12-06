package com.gym.member_service.Dto.MemberTrainerDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListOfMemberIdRequestDto {
    private List<String> memberIds;
}
