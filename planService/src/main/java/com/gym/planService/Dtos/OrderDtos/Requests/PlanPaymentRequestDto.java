package com.gym.planService.Dtos.OrderDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanPaymentRequestDto {

    private String userName;
    private String userId;
    private String planId;
    private String currency;
    private Double amount;
    private String cuponCode;
    private LocalDateTime paymentDate;
}
