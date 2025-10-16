package com.gym.planService.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * Entity representing a subscription or service Plan.
 * <p>
 * Each Plan has a unique name, price, duration, and set of features.
 * A Plan may have multiple coupon codes associated with it.
 * </p>
 *
 * <p><b>Indexes:</b></p>
 * <ul>
 *   <li>Primary key on <code>planId</code></li>
 *   <li>Unique index on <code>planName</code></li>
 *   <li>Index on <code>planName</code> to optimize search queries</li>
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
@Table(name = "Plans", indexes = {
        @Index(name = "idx_plan_name", columnList = "planName")
})
public class Plan {


    /**
     * Unique identifier for the Plan.
     */
    @Id
    @Column(name = "plan_id", nullable = false, unique = true)
    private String planId;

    /**
     * Human-readable name of the Plan.
     */
    @Column(name = "plan_name", nullable = false, unique = true)
    private String planName;

    /**
     * Price charged for the Plan.
     */
    @Column(name = "plan_price", nullable = false)
    private Double planPrice;

    /**
     * Duration of the Plan, e.g. in months.
     */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    /**
     * List of features included in the Plan.
     * <p>
     * This field is stored as a JSON string or a serialized list depending on JPA provider.
     * Consider custom converter if needed.
     * </p>
     */
    @Column(name = "features", nullable = false)
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> features;

    @Column(name = "count")
    private Integer membersCount;
}
