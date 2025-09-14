package com.gym.member_service.Dto.MemberManagementDto.Responses;

import com.gym.member_service.Dto.MemberFitDtos.Responses.DailyRoutineResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.WeightBmiEntryResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class MemberDetailsResponseDto {
    private String id;
    private String profileImageUrl;
    private String name;
    private String email;
    private String phone;
    private boolean frozen;
    private LocalDateTime planExpiration;
    private List<WeightBmiEntryResponseDto> weightBmiEntries;
    private List<MemberPrProgressResponseDto> prProgresses;
    private List<DailyRoutineResponseDto> dailyRoutines;
}
