package com.gym.planService.Services.PlanServices;

import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.Plan;
import com.gym.planService.Repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanMatrixService {

    private final PlanRepository planRepository;

    public String getActiveUsersCount(String planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(()-> new PlanNotFoundException("No plan Found with the id::"+planId));
        log.warn("No plan found with this id::{}",planId);
        return plan.getMembersCount().toString();
    }

    public List<String> getMostPopularPlan() {
        List<Plan> plans = planRepository.findMostPopularPlans();
        log.info("fetched {} plans from db",plans.size());
        return plans.stream().map(Plan::getPlanId).toList();
    }
}
