package com.gym.member_service.Dto.MemberManagementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllMemberListResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private LocalDateTime planExpiration;
    private boolean frozen;
    private int planDurationLeft;
    private String planName;
    private String planId;
    private String profileImageUrl;
    private boolean active;
}
