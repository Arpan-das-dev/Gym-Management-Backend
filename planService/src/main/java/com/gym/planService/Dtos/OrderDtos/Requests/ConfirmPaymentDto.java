package com.gym.planService.Dtos.OrderDtos.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmPaymentDto {
    private String orderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String userId;
    private String userMail;
}
