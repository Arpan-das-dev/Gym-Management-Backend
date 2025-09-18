package com.gym.adminservice.Dto.Responses;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberRequestResponse {
    private String requestId;
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
