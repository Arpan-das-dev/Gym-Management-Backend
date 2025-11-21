package com.gym.adminservice.Dto.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResolveMessageRequestDto {
    private String requestId;
    private String mailMessage;
    private boolean delete;
}
