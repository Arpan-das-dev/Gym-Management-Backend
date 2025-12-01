package com.gym.authservice.Service;

import com.gym.authservice.Dto.Response.EmailOtpNotificationDto;
import com.gym.authservice.Dto.Response.PhoneOtpNotificationDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Utils.OtpGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Service layer responsible for managing OTP generation, storage, sending, and verification
 * for user email and phone number verification processes.
 *
 * <p>This service provides:
 * <ul>
 *   <li>OTP generation and caching in Redis with expiration</li>
 *   <li>Sending OTP notifications via email and SMS</li>
 *   <li>Reactive verification of OTPs with user validation</li>
 *   <li>Cache synchronization and user verification status updates</li>
 * </ul>
 *
 * <p>Logging is included for key operations including OTP creation, verification attempts,
 * cache updates, and failure handling to ensure traceability and troubleshooting capability.
 *
 * <p>Transactional updates ensure data consistency during verification.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final SignedUpsRepository signedUpsRepository;
    private final OtpGenerationUtil generationUtil;
    private final WebClientService notificationService;
    private final CacheManager cacheManager;
    private final LoginService logiInService;

    /**
     * Stores generated OTP for an email key in Redis with specified TTL.
     *
     * @param key the email to associate the OTP with
     * @param otp the generated OTP string
     * @param ttlSeconds time to live in seconds for OTP expiration
     */
    public void StoreEmailOtp(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set("OTP:EMAIL:" + key, otp, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Stored email OTP in Redis with key OTP:EMAIL:{} (value {})", key,otp);
    }

    /**
     * Stores generated OTP for a phone key in Redis with specified TTL.
     *
     * @param key the phone number to associate the OTP with
     * @param otp the generated OTP string
     * @param ttlSeconds time to live in seconds for OTP expiration
     */
    public void StorePhoneOtp(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set("OTP:PHONE:" + key, otp, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Stored phone OTP in Redis with key OTP:PHONE:{} (value {})", key,otp);
    }

    /**
     * Generates and sends OTP to the specified email address.
     *
     * <p>Generates a 6-digit OTP, stores it with 15-minute expiry, logs the operation,
     * and triggers notification via WebClientService.
     *
     * @param email recipient email address
     * @param name recipient name for personalization
     */
    public void sendEmailOtp(String email,String name) {
        String otpEmail = generationUtil.generateOtp(6);
        StoreEmailOtp(email, otpEmail, 900);
        log.info("Stored email OTP cache in Redis with key {} (OTP value hidden)", email);
        notificationService.sendEmailOtp(new EmailOtpNotificationDto(email, otpEmail, name));
    }

    /**
     * Generates and sends OTP to the specified phone number.
     *
     * <p>Generates a 6-digit OTP, stores it with 15-minute expiry, logs the operation,
     * and triggers notification via WebClientService.
     *
     * @param phone recipient phone number
     * @param name recipient name for personalization
     */
    public void sendPhoneOtp(String phone, String name) {
        String otpPhone = generationUtil.generateOtp(6);
        StorePhoneOtp(phone, otpPhone, 900);
        log.info("Stored phone OTP cache in Redis with key {} (OTP value hidden)", phone);
        notificationService.sendPhoneOtp(new PhoneOtpNotificationDto(phone, otpPhone, name));
    }

    /**
     * Verifies an email OTP against stored value.
     *
     * <p>Fetches user by email, compares provided OTP with cached one,
     * updates email verified status on success, evicts used OTP,
     * and updates cache with latest user info.
     * Logs verification steps and warnings on mismatches.
     *
     * @param key email address to verify
     * @param otp OTP string provided by user
     * @return Mono emitting true on successful verification, false otherwise
     */
    public Mono<Boolean> verifyEmail(String key, String otp) {
        log.info("Checking OTP validation for user with email {}", key);
        return signedUpsRepository.findByEmail(key)
                .switchIfEmpty(Mono.error(new UserNotFoundException("No user exists with email: " + key)))
                .flatMap(user -> {
                    String value = redisTemplate.opsForValue().get("OTP:EMAIL:" + key);
                    log.debug("Requested OTP provided by user: [hidden], stored OTP in Redis: [hidden]");
                    if (otp != null && otp.equals(value)) {
                        deleteEmailOtp(key);
                        user.setEmailVerified(true);
                        return signedUpsRepository.save(user)
                                .doOnSuccess(savedUser -> updateCache(savedUser.getId(), savedUser))
                                .thenReturn(true);
                    }
                    log.warn("OTP validation failed for email: {}", key);
                    return Mono.just(false);
                });
    }

    /**
     * Verifies a phone OTP against stored value.
     *
     * <p>Checks user existence and email verification prerequisite,
     * verifies phone OTP, updates phone verified status on success,
     * deletes OTP from cache, and updates user cache.
     * Logs relevant diagnostic information.
     *
     * @param key phone number to verify
     * @param otp OTP string provided by user
     * @return Mono emitting true on successful verification, false otherwise
     */
    @Transactional
    public Mono<Boolean> verifyPhone(String key, String otp) {
        log.info("Checking OTP validation for user with phone {} for OTP [hidden]", key);
        return signedUpsRepository.findByPhone(key)
                .switchIfEmpty(Mono.error(new UserNotFoundException("No user exists with phone no: " + key)))
                .flatMap(user -> {
                    if (!user.isEmailVerified()) {
                        log.warn("Phone verification attempted before email verification for phone: {}", key);
                        return Mono.error(new RuntimeException("Please verify email before verifying phone"));
                    }

                    String redisKey = "OTP:PHONE:" + key;
                    String value = redisTemplate.opsForValue().get(redisKey);

                    if (otp != null && otp.equals(value)) {
                        deletePhoneOtp(key);
                        user.setPhoneVerified(true);
                        return signedUpsRepository.save(user)
                                .doOnSuccess(savedUser -> updateCache(savedUser.getId(), savedUser))
                                .thenReturn(true);
                    } else {
                        log.warn("OTP validation failed for phone: {}", key);
                        return Mono.just(false);
                    }
                });
    }

    /**
     * Updates cache with the latest user verification data.
     *
     * <p>Maps user entity to DTO and updates 'userInfo' cache.
     * Handles cache update failure by evicting stale cache entry.
     *
     * @param id user ID as cache key
     * @param user user entity with updated data
     */
    private void updateCache(String id, SignedUps user) {
        log.info("Updating cache for user with id {}", id);
        SignupDetailsInfoDto infoDto = logiInService.infoMapper(user);
        try {
            Objects.requireNonNull(cacheManager.getCache("userInfo")).put(id, infoDto);
            log.debug("Cache updated successfully for user id {}", id);
        } catch (Exception e) {
            log.warn("Failed to update cache for user {}. Evicting cache. Error: {}", id, e.getMessage());
            Objects.requireNonNull(cacheManager.getCache("userInfo")).evict(id);
        }
    }

    /**
     * Deletes stored email OTP after verification or expiration.
     *
     * @param key email address key whose OTP should be deleted
     */
    public void deleteEmailOtp(String key) {
        log.debug("Deleting email OTP cache for key OTP:EMAIL:{}", key);
        redisTemplate.delete("OTP:EMAIL:" + key);
    }

    /**
     * Deletes stored phone OTP after verification or expiration.
     *
     * @param key phone number key whose OTP should be deleted
     */
    public void deletePhoneOtp(String key) {
        log.debug("Deleting phone OTP cache for key OTP:PHONE:{}", key);
        redisTemplate.delete("OTP:PHONE:" + key);
    }
}
