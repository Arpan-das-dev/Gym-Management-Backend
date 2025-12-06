package com.gym.member_service.Dto.MemberTrainerDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberStatus {
    private String memberId;
    private boolean active;
}
