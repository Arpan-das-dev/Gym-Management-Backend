package com.gym.authservice.Dto.Response;

import com.gym.authservice.Roles.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SignupDetailsInfoDto {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phone;
    private RoleType role;
    private LocalDate joinDate;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean isApproved;
}
