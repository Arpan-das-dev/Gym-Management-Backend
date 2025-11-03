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
public class AdminCreationRequestDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private boolean isApproved;
    private LocalDate joinDate;
    private String gender;
    private RoleType  role;
}
