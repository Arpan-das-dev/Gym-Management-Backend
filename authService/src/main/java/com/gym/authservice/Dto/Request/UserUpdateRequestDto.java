package com.gym.authservice.Dto.Request;

import com.gym.authservice.Roles.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

    private String email;
    private String id;
    private String password;
    private String phone;
    private RoleType roleType;
}
