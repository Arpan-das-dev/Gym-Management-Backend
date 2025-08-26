package com.gym.authservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class MemberResponseDto {
    private String id;
    private String name;
    private String email;
    private String password;
    private LocalDate joinedOn;

}
