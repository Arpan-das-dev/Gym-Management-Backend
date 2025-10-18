package com.gym.planService.Repositories;

import com.gym.planService.Models.PlanCuponCode;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanCuponCodeRepository extends JpaRepository<PlanCuponCode,String> {
    @Query("SELECT c FROM planservice.PlanCuponCode c where planId = :planId")
    List<PlanCuponCode> findAllByPlanId(@Param("planId") String planId);
}
