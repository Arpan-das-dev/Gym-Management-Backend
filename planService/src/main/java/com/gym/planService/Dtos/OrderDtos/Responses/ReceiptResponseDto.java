package com.gym.planService.Dtos.OrderDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptResponseDto {
    private String planName;
    private Double paidPrice;
    private LocalDate paymentDate;
    private String status;
    private String receiptUrl;
}
