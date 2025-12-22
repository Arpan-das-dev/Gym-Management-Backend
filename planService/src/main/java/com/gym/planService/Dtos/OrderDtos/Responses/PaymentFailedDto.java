package com.gym.planService.Dtos.OrderDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedDto {
    private String subject;
    private String cause;
    private Double amount;
    private String userName;
    private String paymentId;
}
