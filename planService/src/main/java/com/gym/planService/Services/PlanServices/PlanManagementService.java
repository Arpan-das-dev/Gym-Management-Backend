package com.gym.planService.Services.PlanServices;

import com.gym.planService.Dtos.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Requests.PlanUpdateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllPlanResponseWrapperResponseDto;
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

/**
 * Service layer responsible for management of subscription plans.
 * <p>
 * Provides business logic for creating, updating, deleting, and retrieving plans.
 * Integrates with the PlanRepository for data persistence.
 * <p>
 * Supports caching to optimize retrieval of all plans.
 * </p>
 *
 * <p>Transactional boundaries ensure consistency during create, update, and delete operations.</p>
 *
 * @author : Arpan Das
 * @version : 1.0
 * @since : 2025-10-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanManagementService {

    private final PlanRepository planRepository;

    /**
     * Creates a new subscription plan if one with the same ID or name does not exist.
     * Saves new plan entity and updates the cache of all plans.
     *
     * @param requestDto plan creation data transfer object
     * @return wrapper DTO containing all plans after creation
     * @throws DuplicatePlanFoundException if a plan with the same ID or name already exists
     */
    @Transactional
    @CachePut(value = "allPlansCache", key = "'all'")
    public AllPlanResponseWrapperResponseDto createPlan(PlanCreateRequestDto requestDto) {
        if (planRepository.existsById(requestDto.getPlanId())) {
            log.warn("Plan ::{} with id ::{} already exists", requestDto.getPlanName(), requestDto.getPlanId());
            throw new DuplicatePlanFoundException("Plan with id:: " + requestDto.getPlanId() + " already exists");
        } else if (planRepository.existsByPlanName(requestDto.getPlanName())) {
            log.warn("Plan already exists with name :: {}", requestDto.getPlanName());
            throw new DuplicatePlanFoundException("Already exists plan with name::" + requestDto.getPlanName());
        }

        Plan plan = Plan.builder()
                .planId(requestDto.getPlanId())
                .planName(requestDto.getPlanName())
                .planPrice(requestDto.getPrice())
                .duration(requestDto.getDuration())
                .features(requestDto.getFeatures())
                .membersCount(0)
                .build();
        planRepository.save(plan);

        log.info("Successfully saved plan::{}", plan.getPlanName());
        return responseWrapperDtoBuilder();
    }

    /**
     * Updates an existing subscription plan identified by ID.
     * Evicts the all plans cache upon update.
     *
     * @param id         plan identifier
     * @param requestDto plan update data transfer object
     * @return updated plan response DTO
     * @throws PlanNotFoundException if no plan is found for the given ID
     */
    @Transactional
    @CacheEvict(value = "allPlansCache", key = "'all'")
    public PlanResponseDto updatePlan(String id, PlanUpdateRequestDto requestDto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException("No plan found with the id::" + id));

        log.info("Successfully retrieved plan -->{}", plan.getPlanName());

        plan.setPlanName(requestDto.getPlanName());
        plan.setPlanPrice(requestDto.getPrice());
        plan.setFeatures(requestDto.getFeatures());
        plan.setDuration(requestDto.getDuration());
        planRepository.save(plan);

        log.info("Successfully saved plan of name::{} with duration of {} days",
                plan.getPlanName(), plan.getDuration());

        return PlanResponseDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .duration(plan.getDuration())
                .price(plan.getPlanPrice())
                .planFeatures(plan.getFeatures())
                .build();
    }

    /**
     * Deletes a subscription plan by ID.
     * Evicts the all plans cache upon deletion.
     *
     * @param id plan identifier to delete
     * @return success or failure message based on deletion outcome
     */
    @Transactional
    @CacheEvict(value = "allPlansCache", key = "'all'")
    public String deletePlan(String id) {
        int rowsEffected = planRepository.deletedById(id);
        if (rowsEffected > 0) {
            log.info("Successfully deleted plan with id::{}", id);
            return "Successfully deleted plan";
        } else {
            log.warn("No plan found with the id::{}", id);
            return "No plan found with the id::" + id;
        }
    }

    /**
     * Retrieves all subscription plans.
     * Utilizes cache for performance.
     *
     * @return wrapper DTO containing list of all plans
     */
    @Cacheable(value = "allPlansCache", key = "'all'")
    public AllPlanResponseWrapperResponseDto getAllPlans() {
        return responseWrapperDtoBuilder();
    }

    /**
     * Constructs a wrapper DTO containing all plans from the repository.
     *
     * @return wrapper containing all plan response DTOs
     */
    private AllPlanResponseWrapperResponseDto responseWrapperDtoBuilder() {
        List<Plan> plans = planRepository.findAll();
        log.info("Successfully retrieved {} plans from database", plans.size());

        List<PlanResponseDto> responseDtoList = plans.stream()
                .map(plan -> PlanResponseDto.builder()
                        .planId(plan.getPlanId())
                        .planName(plan.getPlanName())
                        .price(plan.getPlanPrice())
                        .duration(plan.getDuration())
                        .planFeatures(plan.getFeatures())
                        .build())
                .toList();

        log.info("Successfully built {} plan response objects", responseDtoList.size());

        return AllPlanResponseWrapperResponseDto.builder()
                .responseDtoList(responseDtoList)
                .build();
    }
}
