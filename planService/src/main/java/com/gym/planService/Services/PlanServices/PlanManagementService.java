package com.gym.planService.Services.PlanServices;

import com.gym.planService.Dtos.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Requests.PlanUpdateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllPlanResponseWrapperResponseDto;
import com.gym.planService.Exception.Custom.DuplicatePlanFoundException;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.Plan;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.PlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    private final StringRedisTemplate redisTemplate;
    private final PlanPaymentRepository paymentRepository;
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

        redisTemplate.opsForSet().add("REVENUE:PLANS", plan.getPlanId());
        redisTemplate.opsForValue().set("PLAN:NAME:" + plan.getPlanId(), plan.getPlanName());

        log.info("Plan created and matrix metadata updated | planId={}", plan.getPlanId());
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

        try {
            Plan plan = planRepository.findById(id)
                    .orElseThrow(() -> new PlanNotFoundException("No plan found with id " + id));

            String oldName = plan.getPlanName();

            plan.setPlanName(requestDto.getPlanName());
            plan.setPlanPrice(requestDto.getPrice());
            plan.setFeatures(requestDto.getFeatures());
            plan.setDuration(requestDto.getDuration());

            planRepository.save(plan);

            int effectedRows = paymentRepository.updatePlanName(
                    plan.getPlanId(),
                    oldName,
                    plan.getPlanName()
            );
            log.debug("Updated plan details and rows effected [{}]",effectedRows);
            updatePlanNameCacheSafely(plan.getPlanId(), plan.getPlanName());

            return PlanResponseDto.builder()
                    .planId(plan.getPlanId())
                    .planName(plan.getPlanName())
                    .duration(plan.getDuration())
                    .price(plan.getPlanPrice())
                    .planFeatures(plan.getFeatures())
                    .build();

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicatePlanFoundException(
                    "A plan with name '" + requestDto.getPlanName() + "' already exists"
            );
        }
    }

    private void updatePlanNameCacheSafely(String planId, String planName) {

        String lockKey = "LOCK:PLAN_NAME:" + planId;

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(30));

        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("Plan name cache update skipped due to active lock | planId={}", planId);
            return;
        }

        try {
            String key = "PLAN:NAME:" + planId;

            if (redisTemplate.hasKey(key)) {
                redisTemplate.opsForValue().set(key, planName);
                log.info("Updated plan name in cache | planId={}", planId);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * Deletes a subscription plan by ID.
     * Evicts the all plans cache upon deletion.
     *
     * @param id plan identifier to delete
     * @return success or failure message based on deletion outcome
     */
    @Transactional
    @CachePut(value = "allPlansCache", key = "'all'")
    public AllPlanResponseWrapperResponseDto deletePlan(String id) {
        int rowsEffected = planRepository.deletedById(id);
        if (rowsEffected > 0) {
            log.info("Successfully deleted plan with id::{}", id);
            return responseWrapperDtoBuilder();
        } else {
            log.warn("No plan found with the id::{}", id);
            throw new PlanNotFoundException("No plan found with the id::" + id);
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

    @Caching(evict = {
            @CacheEvict(value = "totalUsers", key = "'totalUsersList'"),
            @CacheEvict(value = "mostPopular", key = "'popular'")
    })
    @Transactional
    public String decrementMemberCount(List<String> planIds) {
        log.info("®️®️ request received in service class for {} plan ids",planIds.size());
        int effectedRows = planRepository.bulkDecrementMemberCount(planIds);
        log.info("Successfully update members count for {} plans",effectedRows);
        if(effectedRows>0){
            return "Successfully decreased member's count for plan ids";
        } else {
            return "Request proceed but no plan found to decrease member's count";
        }
    }


}
