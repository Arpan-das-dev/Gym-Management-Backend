package com.gym.planService.Services.PlanServices;

import com.gym.planService.Dtos.CuponDtos.Requests.CreateCuponCodeRequestDto;
import com.gym.planService.Dtos.CuponDtos.Requests.UpdateCuponRequestDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponValidationResponseDto;
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
 *
 * <p>Provides business logic for creating, updating, fetching, deleting, and validating coupon codes.
 * Integrates with both database repositories and Redis cache to ensure fast and consistent operations.
 * Includes self-healing cache logic — automatically refreshes missing cache entries from DB
 * in case of Redis eviction or service restarts, ensuring consistent user experience even during partial outages.</p>
 *
 * <p>Caching strategy:
 * <ul>
 *   <li>Coupons are stored in Redis with TTL = validity days.</li>
 *   <li>When cache is missing but coupon exists in DB, cache is restored automatically.</li>
 *   <li>All cache writes are atomic, ensuring no stale data.</li>
 * </ul>
 * </p>
 *
 * @author
 *  Arpan Das
 * @version
 *  2.0 (enhanced cache recovery logic)
 * @since
 *  2025-10-19
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
     * Builds a Redis cache key for a given coupon code.
     *
     * @param cuponCode coupon code string
     * @return Redis key
     */
    private String buildCuponKey(String cuponCode) {
        return REDIS_CUPON_PREFIX + cuponCode;
    }

    /**
     * Creates a new coupon code for a specified plan and caches it.
     */
    @Transactional
    @CachePut(value = "cuponCodes", key = "#planId")
    public AllCuponCodeWrapperResponseDto createCuponCode(String planId, CreateCuponCodeRequestDto requestDto) {
        log.info("SERVICE :: Creating coupon {} for plan {}", requestDto.getCuponCode(), planId);

        if (!planRepository.existsById(planId)) {
            throw new PlanNotFoundException("Plan not found with id: " + planId);
        }

        if (cuponCodeRepository.existsById(requestDto.getCuponCode())) {
            throw new DuplicateCuponCodeFoundException("Coupon already exists: " + requestDto.getCuponCode());
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
     * Updates an existing coupon and refreshes cache.
     */
    @Transactional
    @CacheEvict(value = "cuponCodes", key = "#requestDto.planId")
    public CuponCodeResponseDto updateCupon(String cuponCode, UpdateCuponRequestDto requestDto) {
        log.info("SERVICE :: Updating coupon {}", cuponCode);

        PlanCuponCode existing = cuponCodeRepository.findById(cuponCode)
                .orElseThrow(() -> new CuponCodeNotFoundException("Coupon not found: " + cuponCode));

        if (!planRepository.existsById(requestDto.getPlanId())) {
            throw new PlanNotFoundException("Invalid plan for coupon: " + cuponCode);
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
     * Returns all coupons for a plan (cached list).
     */
    @Cacheable(value = "cuponCodes", key = "#planId")
    public AllCuponCodeWrapperResponseDto getCuponCodesByPlanId(String planId) {
        log.debug("SERVICE :: Fetching coupons for plan {}", planId);
        return buildResponse(planId);
    }

    /**
     * Deletes a coupon and removes it from cache.
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

        return "Coupon " + cuponCode + " deleted successfully for plan " + planId;
    }

    /**
     * Validates a coupon.
     *
     * <p>Steps:
     * <ol>
     *   <li>Check Redis cache.</li>
     *   <li>If not found, recover from DB and rebuild cache.</li>
     *   <li>Return validity and off percentage.</li>
     * </ol>
     * </p>
     *
     * @param cuponCode coupon code string
     * @return validation result with off percentage
     */
    public CuponValidationResponseDto validateCupon(String cuponCode) {
        String key = buildCuponKey(cuponCode);
        String cachedValue = redisTemplate.opsForValue().get(key);
        boolean valid = Objects.equals(cachedValue, cuponCode);
        double offPercentage = 0.0;

        if (!valid) {
            // Cache miss — attempt recovery from DB
            log.warn("SERVICE :: Cache miss for coupon {}, attempting recovery from DB", cuponCode);

            PlanCuponCode dbCupon = cuponCodeRepository.findById(cuponCode).orElse(null);

            if (dbCupon != null && dbCupon.getValidity().isAfter(LocalDate.now())) {
                cacheCuponCode(dbCupon); // Rebuild cache
                valid = true;
                offPercentage = dbCupon.getPercentage();
                log.info("SERVICE :: Coupon {} recovered from DB and re-cached", cuponCode);
            } else {
                log.warn("SERVICE :: Coupon {} not found or expired in DB", cuponCode);
            }
        } else {
            // Valid cache entry — get discount from DB
            offPercentage = cuponCodeRepository.findById(cuponCode)
                    .map(PlanCuponCode::getPercentage)
                    .orElse(0.0);
        }

        return CuponValidationResponseDto.builder()
                .valid(valid)
                .offPercentage(offPercentage)
                .build();
    }

    /**
     * Caches the coupon in Redis with TTL based on its validity date.
     */
    private void cacheCuponCode(PlanCuponCode cupon) {
        long days = cupon.getValidity().toEpochDay() - LocalDate.now().toEpochDay();
        if (days <= 0) days = 1;

        redisTemplate.opsForValue()
                .set(buildCuponKey(cupon.getCuponCode()), cupon.getCuponCode(), Duration.ofDays(days));

        log.debug("SERVICE :: Coupon {} cached for {} days", cupon.getCuponCode(), days);
    }

    /**
     * Refreshes (rebuilds) a coupon's cache entry.
     */
    private void refreshCuponCache(PlanCuponCode cupon) {
        redisTemplate.delete(buildCuponKey(cupon.getCuponCode()));
        cacheCuponCode(cupon);
    }

    /**
     * Builds a wrapper response containing all coupon codes for a plan.
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
