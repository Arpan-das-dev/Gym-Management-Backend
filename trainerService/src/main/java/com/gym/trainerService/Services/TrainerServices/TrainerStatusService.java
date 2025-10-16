package com.gym.trainerService.Services.TrainerServices;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service class responsible for managing trainer availability statuses in Redis.
 * <p>
 * This service provides operations to create, retrieve, and delete a trainer's
 * working or availability status. The statuses are stored in Redis using a predefined
 * key prefix for high-performance lookup and temporary caching.
 * </p>
 *
 * <p><b>Storage Details:</b></p>
 * <ul>
 *   <li>Uses Redis string values to store trainer statuses.</li>
 *   <li>Each record expires automatically after 18 hours to ensure data freshness.</li>
 *   <li>Keys follow the format: <code>STATUS::[trainerId]</code>.</li>
 * </ul>
 *
 * <p>This service is typically invoked through the {@link com.gym.trainerService.Controllers.TrainerStatusController}
 * to respond to REST API requests.</p>
 *
 * @author Arpan Das
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerStatusService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Prefix used for all trainer status keys in Redis storage.
     * <p>
     * Ensures consistent naming convention and easy filtering during retrieval or cleanup.
     * </p>
     */
    private final String RedisKEY_PRE_FIX = "STATUS::";

    /**
     * Stores a trainer's availability status in Redis with an 18-hour time-to-live (TTL).
     * <p>
     * Each new status overwrites the previous entry for the given trainer ID.
     * </p>
     *
     * @param status    the status to assign to the trainer (e.g., "AVAILABLE", "UNAVAILABLE")
     * @param trainerId the unique identifier of the trainer
     * @return the stored status value
     *
     */
    public String setStatus(String status, String trainerId) {
        redisTemplate.opsForValue().set(RedisKEY_PRE_FIX + trainerId, status, Duration.ofHours(18));
        log.info("Status is set as :: {} for trainer :: {}", status, trainerId);
        return status;
    }

    /**
     * Retrieves the current status of a specific trainer from Redis.
     * <p>
     * If no value is found in Redis (expired or not set), this method defaults
     * the return status to "UNAVAILABLE".
     * </p>
     *
     * @param trainerId the unique identifier of the trainer
     * @return the trainer's status, or "UNAVAILABLE" if not present in Redis
     *
     */
    public String getStatus(String trainerId) {
        String value = redisTemplate.opsForValue().get(RedisKEY_PRE_FIX + trainerId);
        log.info("Fetched status as :: {} from database for trainer :: {}", value, trainerId);
        if (value == null) {
            return "UNAVAILABLE";
        }
        return value;
    }

    /**
     * Deletes a trainer's status record from Redis.
     * <p>
     * After deletion, the trainer is implicitly marked as "UNAVAILABLE".
     * </p>
     *
     * @param trainerId the unique identifier of the trainer whose status will be removed
     * @return always returns "UNAVAILABLE" indicating post-deletion state
     *
     */
    public String deleteStatus(String trainerId) {
        redisTemplate.delete(RedisKEY_PRE_FIX + trainerId);
        log.info("Deleted status for trainer :: {} and marked as :: {}", trainerId, "UNAVAILABLE");
        return "UNAVAILABLE";
    }
}
