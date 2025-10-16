package com.gym.planService.Services;

import com.gym.planService.Dtos.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Requests.PlanUpdateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllPlanResponseWrapperDto;
import com.gym.planService.Exception.Custom.DuplicatePlanFoundException;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.Plan;
import com.gym.planService.Repositories.PlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanManagementService {

    private final PlanRepository planRepository;

    @Transactional
    @CachePut(value = "allPlansCache", key = "'all'")
    public AllPlanResponseWrapperDto createPlan( PlanCreateRequestDto requestDto) {
        if(planRepository.existsById(requestDto.getPlanId())) {
            log.warn("plan ::{} with id ::{} already exists",requestDto.getPlanName(),requestDto.getPlanId());
           throw new DuplicatePlanFoundException("Plan with id:: "+requestDto.getPlanId() +" already exists");
        } else if (planRepository.existsByPlanName(requestDto.getPlanName())) {
            log.warn("plan already exists with name :: {}",requestDto.getPlanName());
            throw new DuplicatePlanFoundException("Already exists plan with name::" + requestDto.getPlanName());
        }
        Plan plan = Plan.builder()
                .planId(requestDto.getPlanId())
                .planName(requestDto.getPlanName())
                .planPrice(requestDto.getPrice())
                .duration(requestDto.getDuration())
                .features(requestDto.getFeatures())
                .build();
        log.info("Successfully saved plan::{}",plan.getPlanName());
        planRepository.save(plan);
        return responseWrapperDtoBuilder();
    }

    @Transactional
    @CacheEvict(value = "allPlansCache", key = "'all'")
    public PlanResponseDto updatePlan(String id, PlanUpdateRequestDto requestDto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(()-> new PlanNotFoundException("No plan found with the id::"+id));
        log.info("Successfully retrieved plan -->{}",plan.getPlanName());
        plan.setPlanName(requestDto.getPlanName());
        plan.setPlanPrice(requestDto.getPrice());
        plan.setFeatures(requestDto.getFeatures());
        plan.setDuration(requestDto.getDuration());
        planRepository.save(plan);
        log.info("Successfully saved plan of name::{} with duration of {} days",
                plan.getPlanName(),plan.getDuration());
        return PlanResponseDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .duration(plan.getDuration())
                .price(plan.getPlanPrice())
                .planFeatures(plan.getFeatures())
                .build();
    }

    @Transactional
    @CacheEvict(value = "allPlansCache", key = "'all'")
    public String deletePlan(String id) {
        int rowsEffected = planRepository.deletedById(id);
        return rowsEffected > 0 ? "Successfully deleted plan" : "No plan found with the id::"+id;
    }

    @Cacheable(value = "allPlansCache", key = "'all'")
    public AllPlanResponseWrapperDto getAllPlans() {
        return responseWrapperDtoBuilder();
    }

    private AllPlanResponseWrapperDto responseWrapperDtoBuilder() {
        List<Plan> plans = planRepository.findAll();
        log.info("Successfully retrieved {} no of plan from database",plans.size());
        List<PlanResponseDto> responseDtoList = plans.stream()
                .map(plan-> PlanResponseDto.builder()
                        .planId(plan.getPlanId())
                        .planName(plan.getPlanName())
                        .price(plan.getPlanPrice())
                        .duration(plan.getDuration())
                        .planFeatures(plan.getFeatures())
                        .build()).toList();
        log.info("Successfully built {} no of responses",responseDtoList.size());
        return AllPlanResponseWrapperDto.builder()
                .responseDtoList(responseDtoList)
                .build();
    }
}
