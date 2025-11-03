package com.gym.adminservice.Dto.Responses;

import com.gym.adminservice.Enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class SignupRequestDto {
    
    private String  firstName;
   
    private String  lastName;
   
    private String email;
   
   
    private String phone;
   
   
    private String password;
    
    private String gender;
   
    private RoleType role;
    
    private LocalDate joinDate;
}
