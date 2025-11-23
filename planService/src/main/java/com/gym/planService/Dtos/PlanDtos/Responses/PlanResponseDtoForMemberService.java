package com.gym.planService.Dtos.PlanDtos.Responses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponseDtoForMemberService {
    @NotBlank(message = "plan id is required")
    private String planId;

    @NotBlank(message = "plan name is required")
    private String planName;

    @NotNull(message = "Duration can not be empty")
    private Integer duration;
}
