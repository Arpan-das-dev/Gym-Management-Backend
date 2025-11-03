package com.gym.authservice.Dto.Request;

import com.gym.authservice.Roles.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCreationRequestDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private boolean isApproved;
    private LocalDate joinDate;
    private String gender;
    private RoleType role;
}
