package com.gym.planService.Dtos.OrderDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanPaymentRequestDto {

    private String userId;
    private String planId;
    private String currency;
    private Double amount;
    private String cuponCode;
    private LocalDate paymentDate;
}
