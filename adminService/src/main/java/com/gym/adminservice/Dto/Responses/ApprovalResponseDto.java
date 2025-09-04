package com.gym.adminservice.Dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApprovalResponseDto {
    private String email;
    private boolean approval;
}
