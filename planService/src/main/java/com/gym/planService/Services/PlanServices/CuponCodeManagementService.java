package com.gym.planService.Services;

import com.gym.planService.Dtos.CuponDtos.Requests.CreateCuponCodeRequestDto;
import com.gym.planService.Dtos.CuponDtos.Requests.UpdateCuponRequestDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Exception.Custom.CuponCodeNotFoundException;
import com.gym.planService.Exception.Custom.DuplicateCuponCodeFoundException;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.PlanCuponCode;
import com.gym.planService.Repositories.PlanCuponCodeRepository;
import com.gym.planService.Repositories.PlanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service layer for management of coupon codes associated with plans.
 * <p>
 * Provides business logic for creating, updating, fetching, deleting, and validating coupon codes.
 * It integrates with the database repositories and Redis cache to ensure fast and consistent data operations.
 * </p>
 * <p>
 * Includes caching strategies with Spring Cache and Redis to optimize repeated lookups.
 * Proper exception handling ensures robust application behavior.
 * </p>
 *
 * @author : Arpan Das
 * @version : 1.0
 * @since : 2025-10-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CuponCodeManagementService {

    private final PlanRepository planRepository;
    private final PlanCuponCodeRepository cuponCodeRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_CUPON_PREFIX = "VALIDATION_CUPON::";

    /**
     * Construct the Redis cache key for a given coupon code.
     *
     * @param cuponCode coupon code string
     * @return the Redis cache key for the coupon
     */
    private String buildCuponKey(String cuponCode) {
        return REDIS_CUPON_PREFIX + cuponCode;
    }

    /**
     * Creates a new coupon code for a specified plan.
     * Validates existence of the plan and uniqueness of the coupon.
     * Saves the coupon to the database and caches it with appropriate expiry.
     * Evicts existing coupon cache for the plan before returning updated list.
     *
     * @param planId     the ID of the plan
     * @param requestDto coupon creation details
     * @return all coupon codes associated with the plan after creation
     */
    @Transactional
    @CachePut(value = "cuponCodes", key = "#planId")
    public AllCuponCodeWrapperResponseDto createCuponCode(String planId, CreateCuponCodeRequestDto requestDto) {
        log.info("SERVICE :: Creating coupon {} for plan {}", requestDto.getCuponCode(), planId);

        if (!planRepository.existsById(planId)) {
            log.warn("SERVICE :: Plan not found with ID {}", planId);
            throw new PlanNotFoundException("Plan not found with id: " + planId);
        }

        if (cuponCodeRepository.existsById(requestDto.getCuponCode())) {
            log.warn("SERVICE :: Duplicate coupon found {}", requestDto.getCuponCode());
            throw new DuplicateCuponCodeFoundException("Coupon code already exists: " + requestDto.getCuponCode());
        }

        PlanCuponCode entity = PlanCuponCode.builder()
                .cuponCode(requestDto.getCuponCode())
                .validity(requestDto.getValidity())
                .percentage(requestDto.getOffPercentage())
                .planId(planId)
                .build();

        cuponCodeRepository.save(entity);
        cacheCuponCode(entity);

        log.info("SERVICE :: Coupon {} created successfully", entity.getCuponCode());
        return buildResponse(planId);
    }

    /**
     * Updates an existing coupon code's validity and discount.
     * Validates coupon and plan existence, updates DB and refreshes cache.
     * Evicts coupon list cache for corresponding plan.
     *
     * @param cuponCode  coupon code string to update
     * @param requestDto coupon update details
     * @return updated coupon response DTO
     */
    @Transactional
    @CacheEvict(value = "cuponCodes", key = "#requestDto.planId")
    public CuponCodeResponseDto updateCupon(String cuponCode, UpdateCuponRequestDto requestDto) {
        log.info("SERVICE :: Updating coupon {}", cuponCode);

        PlanCuponCode existing = cuponCodeRepository.findById(cuponCode)
                .orElseThrow(() -> new CuponCodeNotFoundException("Coupon not found: " + cuponCode));

        if (!planRepository.existsById(requestDto.getPlanId())) {
            log.warn("SERVICE :: Invalid plan id {} for coupon {}", requestDto.getPlanId(), cuponCode);
            throw new PlanNotFoundException("No plan found for coupon: " + requestDto.getPlanId());
        }

        existing.setValidity(requestDto.getValidity());
        existing.setPercentage(requestDto.getOffPercentage());
        cuponCodeRepository.save(existing);

        refreshCuponCache(existing);
        log.info("SERVICE :: Coupon {} updated successfully", cuponCode);

        return CuponCodeResponseDto.builder()
                .cuponCode(existing.getCuponCode())
                .offPercentage(existing.getPercentage())
                .validityDate(existing.getValidity())
                .build();
    }

    /**
     * Fetches all coupon codes associated with a plan.
     * Uses cache if available.
     *
     * @param planId the plan ID
     * @return all coupon codes wrapped in response DTO
     */
    @Cacheable(value = "cuponCodes", key = "#planId")
    public AllCuponCodeWrapperResponseDto getCuponCodesByPlanId(String planId) {
        log.debug("SERVICE :: Fetching coupons for plan {}", planId);
        return buildResponse(planId);
    }

    /**
     * Deletes a coupon code by its code and plan ID.
     * Updates database and removes coupon from Redis cache.
     * Evicts cached coupon list for the plan.
     *
     * @param cuponCode coupon code to delete
     * @param planId    plan ID associated with the coupon
     * @return success confirmation message
     */
    @Transactional
    @CacheEvict(value = "cuponCodes", key = "#planId")
    public String deleteCuponByCuponCode(String cuponCode, String planId) {
        log.info("SERVICE :: Deleting coupon {} for plan {}", cuponCode, planId);

        if (!cuponCodeRepository.existsById(cuponCode)) {
            throw new CuponCodeNotFoundException("Coupon not found: " + cuponCode);
        }

        cuponCodeRepository.deleteById(cuponCode);
        redisTemplate.delete(buildCuponKey(cuponCode));

        log.info("SERVICE :: Coupon {} deleted successfully and removed from cache", cuponCode);
        return "Coupon " + cuponCode + " deleted successfully for plan " + planId;
    }

    /**
     * Validates if the coupon code is valid by checking Redis cache.
     *
     * @param cuponCode coupon code string
     * @return true if valid, false otherwise
     */
    public boolean validateCupon(String cuponCode) {
        String key = buildCuponKey(cuponCode);
        String cachedValue = redisTemplate.opsForValue().get(key);

        boolean valid = Objects.equals(cachedValue, cuponCode);
        log.debug("SERVICE :: Coupon {} validation result: {}", cuponCode, valid);
        return valid;
    }

    /**
     * Caches the coupon code in Redis with expiration based on validity.
     *
     * @param cupon coupon entity to cache
     */
    private void cacheCuponCode(PlanCuponCode cupon) {
        long days = cupon.getValidity().toEpochDay() - LocalDate.now().toEpochDay();
        if (days <= 0) {
            days = 1;
        }
        redisTemplate.opsForValue()
                .set(buildCuponKey(cupon.getCuponCode()), cupon.getCuponCode(), Duration.ofDays(days));
        log.debug("SERVICE :: Coupon {} cached for {} days", cupon.getCuponCode(), days);
    }

    /**
     * Refreshes the coupon cache by deleting and resetting it.
     *
     * @param cupon coupon entity to refresh cache for
     */
    private void refreshCuponCache(PlanCuponCode cupon) {
        redisTemplate.delete(buildCuponKey(cupon.getCuponCode()));
        cacheCuponCode(cupon);
    }

    /**
     * Builds a response DTO wrapping a list of coupon codes for a plan.
     *
     * @param planId the plan ID
     * @return all coupons wrapped into AllCuponCodeWrapperResponseDto
     */
    private AllCuponCodeWrapperResponseDto buildResponse(String planId) {
        List<CuponCodeResponseDto> codes = cuponCodeRepository.findAllByPlanId(planId).stream()
                .map(c -> CuponCodeResponseDto.builder()
                        .cuponCode(c.getCuponCode())
                        .validityDate(c.getValidity())
                        .offPercentage(c.getPercentage())
                        .build())
                .toList();

        return AllCuponCodeWrapperResponseDto.builder()
                .responseDtoList(codes)
                .build();
    }
}
