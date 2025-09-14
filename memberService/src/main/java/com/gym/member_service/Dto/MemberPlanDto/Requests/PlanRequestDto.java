package com.gym.member_service.Dto.MemberPlanDto.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanRequestDto {

    @NotBlank(message = "plan id is required")
    private String planId;

    @NotBlank(message = "plan name is required")
    private String planName;

    @NotNull(message = "Duration can not be empty")
    private Integer duration;
}
