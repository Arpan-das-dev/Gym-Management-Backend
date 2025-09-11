package com.gym.member_service.MemberManagementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate joinDate;
    private double currentBmi;
    private Integer loginStreak;
    private LocalDateTime planExpiration;
    private boolean frozen;
}
