package com.gym.planService.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class PlanPayment {

    @Id
    private String paymentId;
    private Double paidPrice;
    private String currency;
    private LocalDate paymentDate;
    private String paymentMonth;
    private Integer paymentYear;
}
