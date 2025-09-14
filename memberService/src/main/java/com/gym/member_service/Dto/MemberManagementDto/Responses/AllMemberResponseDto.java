package com.gym.member_service.Dto.MemberManagementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AllMemberResponseDto {
    private String id;
    private String imageUrl;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private double currentBmi;
    private Integer loginStreak;
    private LocalDateTime planExpiration;
    private boolean frozen;
}
