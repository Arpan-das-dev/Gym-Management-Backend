package com.gym.planService.Models;

import jakarta.persistence.Column;
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
public class MonthlyRevenue {

    @Id
    private LocalDate month;

    @Column(name = "revenue")
    private Double monthlyRevenue;

    @Column(name = "receipt_url")
    private String receiptUrl;

    private String currentMonth;

    private Integer currentYear;

}
