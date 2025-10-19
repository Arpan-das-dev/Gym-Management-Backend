package com.gym.planService.Repositories;

import com.gym.planService.Models.PlanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanPaymentRepository extends JpaRepository<PlanPayment,String> {
}
