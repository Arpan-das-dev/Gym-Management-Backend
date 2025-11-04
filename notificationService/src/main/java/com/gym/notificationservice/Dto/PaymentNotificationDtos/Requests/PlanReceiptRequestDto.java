package com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanReceiptRequestDto {
    private String userName;
    private String userMail;
    private String planName;
    private Integer planDuration;
    private Double planPrice;
}
