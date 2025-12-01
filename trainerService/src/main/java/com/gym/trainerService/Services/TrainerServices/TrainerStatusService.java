package com.gym.trainerService.Services.TrainerServices;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
/**
 * Service responsible for managing trainer availability statuses in Redis.
 *
 * <p>This service provides high-performance status handling for trainers:
 * <ul>
 *     <li>Stores status in Redis with TTL</li>
 *     <li>Maintains a Redis Set of active trainers (no duplicates)</li>
 *     <li>Broadcasts real-time active trainer count via WebSocket</li>
 * </ul>
 *
 * <p><b>Key Storage:</b></p>
 * <ul>
 *     <li>Status key format: {@code STATUS::<trainerId>}</li>
 *     <li>Active trainer set key: {@code trainerCountCache}</li>
 *     <li>Status TTL: 18 hours</li>
 * </ul>
 *
 * <p>Controller is responsible for validating allowed status values.</p>
 *
 * @author Arpan
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerStatusService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate ws;

    /** Prefix for trainer status keys in Redis. */
    private static final String STATUS_KEY_PREFIX = "STATUS::";

    /** Redis set that stores active trainers (AVAILABLE). */
    private static final String ACTIVE_SET_KEY = "trainerCountCache";

    /** TTL for each trainer's status record. */
    private static final Duration STATUS_TTL = Duration.ofHours(18);

    /**
     * Stores a trainer's availability status in Redis.
     *
     * <p>Automatic behaviors:
     * <ul>
     *     <li>Overwrites previous status</li>
     *     <li>Expires after 18 hours</li>
     *     <li>Updates active-trainer set (AVAILABLE vs UNAVAILABLE)</li>
     * </ul>
     *
     * @param status    trainer status (validated upstream)
     * @param trainerId trainer unique identifier
     * @return stored status
     */
    public String setStatus(String status, String trainerId) {

        String redisKey = STATUS_KEY_PREFIX + trainerId;

        redis.opsForValue().set(redisKey, status, STATUS_TTL);

        // ACTIVE → add to set, UNAVAILABLE → remove
        if ("UNAVAILABLE".equalsIgnoreCase(status) || "BUSY".equalsIgnoreCase(status)) {
            markAsInactive(trainerId);
        } else {
            markAsActive(trainerId);
        }

        log.info("📝 Status updated: trainer={} → {}", trainerId, status);
        return status;
    }

    /**
     * Retrieves a trainer's current status from Redis.
     *
     * <p>If no status exists (expired or never set), defaults to {@code UNAVAILABLE}.
     *
     * @param trainerId trainer identifier
     * @return status or {@code UNAVAILABLE}
     */
    public String getStatus(String trainerId) {
        String value = redis.opsForValue().get(STATUS_KEY_PREFIX + trainerId);

        log.info("📥 Status fetched: trainer={} → {}", trainerId, value);

        return value == null ? "UNAVAILABLE" : value;
    }

    /**
     * Deletes the trainer's status from Redis.
     *
     * <p>After deletion, the trainer is considered UNAVAILABLE.
     *
     * @param trainerId trainer identifier
     * @return always {@code UNAVAILABLE}
     */
    public String deleteStatus(String trainerId) {
        redis.delete(STATUS_KEY_PREFIX + trainerId);
        markAsInactive(trainerId); // ensures count updates
        log.info("🗑 Deleted status for trainer={}, defaulting to UNAVAILABLE", trainerId);
        return "UNAVAILABLE";
    }


    /**
     * Checks whether the given trainer ID belongs to the active (AVAILABLE) set.
     *
     * @param trainerId trainer identifier
     * @return true if active, false otherwise
     */
    public boolean isActive(String trainerId) {
        return Boolean.TRUE.equals(redis.opsForSet().isMember(ACTIVE_SET_KEY, trainerId));
    }

    /**
     * Returns the number of trainers currently marked as AVAILABLE.
     *
     * @return active trainer count
     */
    public Long getActiveTrainersCount() {
        Long count = redis.opsForSet().size(ACTIVE_SET_KEY);
        return count == null ? 0L : count;
    }

    /**
     * Broadcasts the current number of active trainers over a WebSocket topic.
     */
    public void broadcastLiveCount() {
        Long currentActive = getActiveTrainersCount();
        ws.convertAndSend("/topic/activeTrainers", currentActive);
        log.info("📢 Live Active Count Broadcast → {}", currentActive);
    }

    /**
     * Marks a trainer as active by adding them to the active set.
     *
     * <p>Prevents duplicates by checking membership first.</p>
     *
     * @param trainerId trainer identifier
     */
    public void markAsActive(String trainerId) {
        if (!isActive(trainerId)) {
            redis.opsForSet().add(ACTIVE_SET_KEY, trainerId);
            setStatus("AVAILABLE",trainerId);
            log.info("🤸 Trainer ACTIVE: {} (broadcasting new count…)", trainerId);
            broadcastLiveCount();
        }
    }

    /**
     * Marks a trainer as inactive by removing them from the active set.
     *
     * @param trainerId trainer identifier
     */
    public void markAsInactive(String trainerId) {
        if (isActive(trainerId)) {
            redis.opsForSet().remove(ACTIVE_SET_KEY, trainerId);
            setStatus("UNAVAILABLE",trainerId);
            log.info("🛑 Trainer INACTIVE: {} (broadcasting new count…)", trainerId);
            broadcastLiveCount();
        }
    }
}