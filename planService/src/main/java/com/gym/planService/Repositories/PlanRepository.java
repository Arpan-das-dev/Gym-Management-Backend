package com.gym.planService.Repositories;

import com.gym.planService.Models.Plan;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for Plan entity CRUD operations and custom queries.
 * <p>
 * Extends JpaRepository to provide basic CRUD and pagination features.
 * Includes additional method to check existence by plan name and custom deletion query by plan ID.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   boolean exists = planRepository.existsByPlanName("Premium");
 *   int deletedCount = planRepository.deletedById("plan123");
 * </pre>
 *
 * @author : Arpan Das
 * @version : 1.0
 * @since : 2025-10-19
 */

public interface PlanRepository extends JpaRepository< Plan, String > {

    /**
     * Checks if a plan exists with the specified name.
     *
     * @param planName the name of the plan
     * @return true if a plan exists with the given name; false otherwise
     */
    boolean existsByPlanName(String planName);

    /**
     * Deletes a plan by its identifier.
     * Uses a custom JPQL query with the @Modifying annotation.
     *
     * @param id the plan identifier
     * @return the number of rows affected (typically 1 if deleted, 0 if not found)
     */
    @Modifying
    @Query("DELETE FROM Plan p WHERE p.id = :id")
    int deletedById(@Param("id") String id);

    @Query("SELECT p FROM Plan p WHERE p.membersCount = (SELECT MAX(p2.membersCount) FROM Plan p2)")
    List<Plan> findMostPopularPlans();

    @Query("SELECT SUM(p.membersCount) FROM Plan p")
    Integer findTotalUsers();

    @Query("SELECT COUNT(p) > 0 FROM Plan p WHERE p.planId = :planId")
    boolean exists(@Param("planId") String planId);

}
