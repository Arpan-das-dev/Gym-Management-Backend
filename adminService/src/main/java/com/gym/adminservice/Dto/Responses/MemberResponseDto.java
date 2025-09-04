package com.gym.adminservice.Dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@Builder
public class MemberResponseDto {

    private String  firstName;
    private String  lastName;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private LocalDate joinDate;
    private String address;
    private Byte age;
}
