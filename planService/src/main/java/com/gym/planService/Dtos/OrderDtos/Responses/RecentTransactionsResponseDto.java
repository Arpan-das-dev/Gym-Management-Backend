package com.gym.planService.Dtos.OrderDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentTransactionsResponseDto {

    private String paymentId;
    private String userName;
    private String userId;
    private String planName;
    private Double paidPrice;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDate paymentDate;
    private LocalDateTime  paymentTime;
}
