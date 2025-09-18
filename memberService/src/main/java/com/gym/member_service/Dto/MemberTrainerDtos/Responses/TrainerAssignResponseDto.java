package com.gym.member_service.Dto.MemberTrainerRequestDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Setter
@Builder
@NoArgsConstructor
/**
 * This response send data through webclient to admin service
 * for request approval
 */
public class TrainerAssignResponseDto {
    private String memberId;
    private String memberProfileImageUrl;
    private String memberName;
    private String trainerId;
    private String trainerProfileImageUrl;
    private String TrainerName;
    private LocalDate requestDate;
    private String planName;
    private LocalDate memberPlanExpirationDate;
}
