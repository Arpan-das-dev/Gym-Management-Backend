package com.gym.authservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class MemberCreationResponseDto {
    private String  firstName;
    private String  lastName;
    private String email;
    private String id;
    private String phone;
    private String gender;
    private LocalDate joinDate;
}
