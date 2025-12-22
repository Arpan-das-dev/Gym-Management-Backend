package com.gym.planService.Services.PlanServices;

import com.gym.planService.Dtos.CuponDtos.Requests.CreateCuponCodeRequestDto;
import com.gym.planService.Dtos.CuponDtos.Requests.UpdateCuponRequestDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponValidationResponseDto;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Enums.AccessType;
import com.gym.planService.Exception.Custom.CuponCodeCreationException;
import com.gym.planService.Exception.Custom.CuponCodeNotFoundException;
import com.gym.planService.Exception.Custom.DuplicateCuponCodeFoundException;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.Plan;
import com.gym.planService.Models.PlanCuponCode;
import com.gym.planService.Repositories.PlanCuponCodeRepository;
import com.gym.planService.Repositories.PlanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * Builds a Redis cache key for a given coupon code.
     *
     * @param cuponCode coupon code string
     * @return Redis key
     */
    private String buildCuponKey(String cuponCode) {
        return REDIS_CUPON_PREFIX + cuponCode+"_";
    }

    /**
     * Creates a new coupon code for a specified plan and caches it.
     */
    @Transactional
    @Caching(put = {
            @CachePut(value = "cuponCodes", key = "#planId"),

    },
    evict = {
            @CacheEvict(value = "cuponCodes", key = "'public'"),
            @CacheEvict(value = "cuponCodes", key = "'admin'"),
    })
    public AllCuponCodeWrapperResponseDto createCuponCode(String planId, CreateCuponCodeRequestDto requestDto) {
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.info("SERVICE :: Creating coupon {} for plan {}", requestDto.getCuponCode(), planId);
        log.info("access is {}",requestDto.getAccess());

        if (!planRepository.exists(planId)) {
            throw new PlanNotFoundException("Plan not found with id: " + planId);
        }

        if (cuponCodeRepository.existsById(requestDto.getCuponCode())) {
            throw new DuplicateCuponCodeFoundException("Coupon already exists with name: " + requestDto.getCuponCode());
        }

        if(!requestDto.getAccess().toUpperCase().equals(AccessType.PRIVATE.name()) &&
                !requestDto.getAccess().toUpperCase().equals(AccessType.PUBLIC.name())){
            throw new CuponCodeCreationException("can not create a cupon without setting access eg: PUBLIC, PRIVATE");
        }
        PlanCuponCode entity = PlanCuponCode.builder()
                .cuponCode(requestDto.getCuponCode())
                .accessibility(requestDto.getAccess().toUpperCase())
                .description(requestDto.getDescription())
                .validFrom(requestDto.getValidFrom())
                .validity(requestDto.getValidity())
                .percentage(requestDto.getOffPercentage())
                .planId(planId)
                .cuponCodeUser(0)
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
    @Caching(evict = {
            @CacheEvict(value = "cuponCodes", key = "#requestDto.planId"),
            @CacheEvict(value = "cuponCodes", key = "'admin'"),
            @CacheEvict(value = "cuponCodes", key = "'public'")
    })
    public CuponCodeResponseDto updateCupon(String cuponCode, UpdateCuponRequestDto requestDto) {
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.info("SERVICE :: Updating coupon {}", cuponCode);

        PlanCuponCode existing = cuponCodeRepository.findById(cuponCode)
                .orElseThrow(() -> new CuponCodeNotFoundException("Coupon not found: " + cuponCode));

        if (!planRepository.existsById(requestDto.getPlanId())) {
            throw new PlanNotFoundException("Invalid plan for coupon: " + cuponCode);
        }
        if (
                !requestDto.getAccess().equalsIgnoreCase(AccessType.PRIVATE.name()) &&
                        !requestDto.getAccess().equalsIgnoreCase(AccessType.PUBLIC.name())
        ) {
            throw new CuponCodeCreationException(
                    "Can not create a coupon without valid access type: PUBLIC or PRIVATE"
            );
        }

        existing.setValidFrom(requestDto.getValidFrom());
        existing.setValidity(requestDto.getValidity());
        existing.setPercentage(requestDto.getOffPercentage());
        existing.setAccessibility(requestDto.getAccess().toUpperCase());
        existing.setDescription(requestDto.getDescription());
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
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.debug("SERVICE :: Fetching coupons for plan {}", planId);
        return buildResponse(planId);
    }

    /**
     * Deletes a coupon and removes it from cache.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cuponCodes", key = "#planId"),
            @CacheEvict(value = "cuponCodes", key = "'admin'"),
            @CacheEvict(value = "cuponCodes", key = "'public'")
    })
    public String deleteCuponByCuponCode(String cuponCode, String planId) {
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
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
    public CuponValidationResponseDto validateCupon(String cuponCode,String PlanId) {
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        String key = buildCuponKey(cuponCode);
        String cachedValue = redisTemplate.opsForValue().get(key);
        boolean valid = false;
        double offPercentage = 0.0;
        if(cachedValue!=null){
            log.info("line-220 {}::-> cachedValue is not null start retrieving operation","CuponCodeManagementService");
            int indexOfUnderScore = cachedValue.indexOf("_");
            String cachedCode = cachedValue.substring(0,indexOfUnderScore);
            String cachedPlanId = cachedValue.substring(indexOfUnderScore+1);

            log.info("Retrieved cupon code [{}] and planId [ {} ] from cache",cachedCode,cachedPlanId);
            boolean validation = cachedCode.equals(cuponCode) && cachedPlanId.equals(PlanId);

            if(validation){
                valid = true;
                offPercentage = cuponCodeRepository.findById(cuponCode)
                        .map(PlanCuponCode::getPercentage)
                        .orElse(0.00);
                log.info("line-234::CuponCodeManagementService Set validation as ->true with percentage [{}%]"
                        ,offPercentage);
            }
        } else{
            log.info("Cache miss for coupon {}. Checking Database...", cuponCode);
            PlanCuponCode code = cuponCodeRepository.findById(cuponCode)
                    .orElseThrow(() -> {
                        log.warn("No cupon code found for --> [{}]",cuponCode);
                        return new PlanNotFoundException("Unable To Find Any CuponCode May Be this is an Old CuponCode");
                    });
            boolean condition = code.getPlanId().equals(PlanId)
                    && code.getValidity().isAfter(LocalDate.now());
            if(condition) {
                cacheCuponCode(code);
                offPercentage = code.getPercentage();
                log.info("line-249::CuponCodeManagementService Set validation as ->true with percentage [{}%]"
                        ,offPercentage);
            }
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
        String value = cupon.getCuponCode()+"_"+cupon.getPlanId();
        String key = buildCuponKey(cupon.getCuponCode());
        redisTemplate.opsForValue()
                .set(key, value, Duration.ofDays(days));

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
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.info(" 📩 Request reached in service class to retrieve all cupon codes by plan id {} ",planId);
        return wrapperBuilder(cuponCodeRepository.findAllByPlanId(planId));
    }

    @Cacheable(value = "cuponCodes", key = "'admin'")
    public AllCuponCodeWrapperResponseDto getAllCuponCodesForAdmin(){
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.info(" 📩 Request reached in service class to retrieve all cupon codes by admin ");
        return wrapperBuilder(cuponCodeRepository.findAll());
    }

    @Cacheable(value = "cuponCodes", key = "'public'")
    public AllCuponCodeWrapperResponseDto getAllPublicCuponCodes(){
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.info(" 📩 Request reached in service class to retrieve all cupon codes for public ");
        return wrapperBuilder(cuponCodeRepository.findPublicCodes());
    }

    private AllCuponCodeWrapperResponseDto wrapperBuilder(List<PlanCuponCode> cuponCodes){
        List<CuponCodeResponseDto> codeResponseDtoList = cuponCodes.stream()
                .map(cupon-> CuponCodeResponseDto.builder()
                        .cuponCode(cupon.getCuponCode())
                        .planId(cupon.getPlanId())
                        .planName(nameMapper(cupon))
                        .access(cupon.getAccessibility())
                        .validFrom(cupon.getValidFrom())
                        .validityDate(cupon.getValidity())
                        .users(cupon.getCuponCodeUser())
                        .offPercentage(cupon.getPercentage())
                        .description(cupon.getDescription())
                        .build()).toList();
        System.out.println("    ⌚ "+ LocalDateTime.now().format(formatter));
        log.info("retrieved {} no of cupon codes from db/ cache ",codeResponseDtoList.size());
        return AllCuponCodeWrapperResponseDto.builder()
                .responseDtoList(codeResponseDtoList)
                .build();
    }

    private String nameMapper(PlanCuponCode cupon) {
        Plan plan = planRepository.findById(cupon.getPlanId()).orElse(null);
        if(plan != null) {
            return  (plan.getPlanName() == null || plan.getPlanName().isEmpty()) ?
                    "" : plan.getPlanName();
        }
        return "";
    }
}
