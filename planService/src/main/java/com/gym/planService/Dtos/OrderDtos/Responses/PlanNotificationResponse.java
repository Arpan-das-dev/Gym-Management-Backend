package com.gym.planService.Dtos.OrderDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanNotificationResponse {

    private String userName;
    private String userMail;
    private String planName;
    private Integer planDuration;
    private Double planPrice;
}
