package com.gym.planService.Repositories;

import com.gym.planService.Models.Plan;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlanRepository extends JpaRepository< Plan, String > {


    boolean existsByPlanName(String planName);

    @Modifying
    @Query("DELETE FROM Plan p WHERE p.id = :id")
    int deletedById(@Param("id") String id);

}
