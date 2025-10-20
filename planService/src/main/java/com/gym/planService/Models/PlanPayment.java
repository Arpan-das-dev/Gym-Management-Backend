package com.gym.planService.Models;

import jakarta.persistence.*;
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
@Entity
@Table(name = "plan_payment", indexes = {
        @Index(name = "idx_payment_user", columnList = "user_id"),
        @Index(name = "idx_payment_plan", columnList = "plan_id"),
        @Index(name = "idx_payment_date", columnList = "payment_date"),
        @Index(name = "idx_payment_month_year", columnList = "payment_month, payment_year")
})
public class PlanPayment {

    @Id
    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "plan_id", nullable = false)
    private String planId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "paid_price", nullable = false)
    private Double paidPrice;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_month", nullable = false)
    private String paymentMonth;

    @Column(name = "payment_year", nullable = false)
    private Integer paymentYear;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // e.g. SUCCESS, FAILED, PENDING, REFUNDED

    @Column(name = "payment_method")
    private String paymentMethod; // e.g. UPI, CARD, NET BANKING, WALLET

    @Column(name = "order_id")
    private String orderId; // Razorpay Order ID (useful for verification or reconciliation)

    @Column(name = "receipt_url")
    private String receiptUrl; // Path or URL to generated PDF receipt

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime; // exact timestamp
}