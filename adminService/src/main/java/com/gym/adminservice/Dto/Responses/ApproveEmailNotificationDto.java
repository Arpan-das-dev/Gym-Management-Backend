package com.gym.adminservice.Dto.Responses;

import com.gym.adminservice.Enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApproveEmailNotificationDto {
    private String email;
    private String name;
    private RoleType role;
}
