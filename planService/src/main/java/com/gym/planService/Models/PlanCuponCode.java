package com.gym.planService.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing a coupon code linked to a specific Plan.
 * <p>
 * Each coupon code has a validity date and discount percentage and is associated
 * with exactly one Plan. Multiple coupons can belong to the same Plan.
 * </p>
 *
 * <p><b>Indexes:</b></p>
 * <ul>
 *   <li>Primary key on <code>cuponCode</code></li>
 *   <li>Foreign key column <code>plan_id</code> indexed implicitly for fast joins</li>
 * </ul>
 *
 * @author Arpan
 * @version 1.0
 * @since 2025-10-16
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cupon",indexes = {
        @Index(name = "idx_cupon_plan_id", columnList = "plan_id"),
        @Index(name = "idx_access", columnList = "Access")
})
public class PlanCuponCode {

    /**
     * Unique coupon code string serving as primary key.
     */
    @Id
    @Column(name = "cupon_code", nullable = false, unique = true)
    private String cuponCode;

    @Column(name = "Access", nullable = false)
    private String accessibility;

    @Column(name = "description")
    private String description;

    @Column(name = "valid", nullable = false)
    private LocalDate validFrom;

    @Column(name = "validity", nullable = false)
    private LocalDate validity;

    /**
     * Discount percentage offered by the coupon.
     */
    @Column(name = "off", nullable = false)
    private Double percentage;


    @Column(name = "plan_id", nullable = false)
    private String planId;

    @Column(name = "users")
    private Integer cuponCodeUser;


}
