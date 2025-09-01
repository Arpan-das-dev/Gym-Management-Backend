package com.gym.authservice.Dto.Response;

import com.gym.authservice.Roles.RoleType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ApproveResponseDto {

    private String email;
    private String phone;
    private String name;
    private RoleType role;
    private LocalDate joinDate;
}
