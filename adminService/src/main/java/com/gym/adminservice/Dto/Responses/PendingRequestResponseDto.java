package com.gym.adminservice.Dto.Responses;

import com.gym.adminservice.Enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingRequestResponseDto {
    private String requestId;
    private String email;
    private String phone;
    private String name;
    private RoleType role;
    private LocalDate joinDate;
}
