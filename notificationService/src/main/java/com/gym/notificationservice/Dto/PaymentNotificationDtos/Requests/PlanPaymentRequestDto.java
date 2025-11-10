package com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String userId;

    @NotBlank
    private String userName;

    @NotBlank
    private String userMail;

    @NotBlank
    private String planId;

    @NotBlank
    private String currency;

    @NotNull
    private Double amount;

    private String cuponCode;

    @NotNull
    private LocalDateTime paymentDate;

}
