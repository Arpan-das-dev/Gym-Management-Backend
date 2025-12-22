package com.gym.planService.Repositories;

import com.gym.planService.Models.PlanCuponCode;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on PlanCuponCode entities.
 * <p>
 * Extends JpaRepository to leverage Spring Data JPA capabilities.
 * Provides custom query method to fetch all coupons associated with a specific plan.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   List&lt;PlanCuponCode&gt; coupons = planCuponCodeRepository.findAllByPlanId("PLAN123");
 * </pre>
 *
 * Author: Arpan Das
 * Version: 1.0
 * Since: 2025-10-19
 */
public interface PlanCuponCodeRepository extends JpaRepository<PlanCuponCode,String> {

    /**
     * Finds all coupon codes linked to a specific plan ID.
     *
     * @param planId the identifier of the plan
     * @return a list of PlanCuponCode entities associated with the plan
     */
    @Query("SELECT c FROM PlanCuponCode c WHERE c.planId = :planId")
    List<PlanCuponCode> findAllByPlanId(@Param("planId") String planId);

    @Query("SELECT c FROM PlanCuponCode c WHERE c.accessibility = 'PUBLIC'")
    List<PlanCuponCode> findPublicCodes();

    @Modifying
    @Query("""
                UPDATE PlanCuponCode c
                SET c.cuponCodeUser = COALESCE(c.cuponCodeUser, 0) + 1
                WHERE c.cuponCode = :cuponCode
            """)
    int incrementCuponUsageCount(@Param("cuponCode") String cuponCode);

}
