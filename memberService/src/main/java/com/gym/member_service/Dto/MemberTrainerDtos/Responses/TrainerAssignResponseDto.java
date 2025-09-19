package com.gym.member_service.Dto.MemberTrainerDtos.Responses;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@Setter
@Getter
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
    private String trainerName;
    private LocalDate requestDate;
    private String memberPlanName;
    private LocalDate memberPlanExpirationDate;
}
