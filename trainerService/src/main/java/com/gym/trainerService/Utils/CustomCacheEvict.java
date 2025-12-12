package com.gym.trainerService.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomCacheEvict{
    private final StringRedisTemplate redisTemplate;
    public void evictTrainerSessionCachePattern(String cacheName, String trainerId, String type) {

        Set<String> keysToDelete = new HashSet<>();

        // MEMBER-FIRST caches (e.g., BMI)
        String trainerPattern = cacheName + "::" + trainerId + "*";

        // Session caches
        String upPattern   = cacheName + "::UP:" + trainerId + "*";
        String pastPattern = cacheName + "::PAST:" + trainerId + "*";

        log.debug("Evicting cache '{}' for trainer '{}' with type '{}'", cacheName, trainerId, type);

        switch (type.toUpperCase()) {

            case "UP":
                keysToDelete.addAll(safeKeys(upPattern));
                break;

            case "PAST":
                keysToDelete.addAll(safeKeys(pastPattern));
                break;

            case "MEMBER":
                keysToDelete.addAll(safeKeys(trainerPattern));
                break;

            case "ALL":  // Clear every related cache
                keysToDelete.addAll(safeKeys(trainerPattern));
                keysToDelete.addAll(safeKeys(upPattern));
                keysToDelete.addAll(safeKeys(pastPattern));
                break;

            default:
                log.warn("Invalid cache eviction type '{}'. Allowed: UP, PAST, MEMBER, ALL", type);
                return;
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("Evicted {} keys from cache '{}' for trainer '{}' for::->{}", keysToDelete.size()
                    , cacheName, trainerId,type);
        } else {
            log.warn("No keys found to evict for cache '{}' and trainer '{}'", cacheName, trainerId);
        }
    }
    /**
     * Custom method to clear all paginated review entries for a specific trainer
     * using Redis KEYS pattern matching.
     * * The cache key is structured as: "reviewCache::[trainerId]:[pageNo]:[pageSize]:[sortDirection]"
     * * @param trainerId The ID of the trainer whose review pages need to be cleared.
     */
    public void evictReviewCacheByTrainerId(String trainerId) {
        final String cacheName = "reviewCache";
        String pattern = cacheName + "::" + trainerId + ":*";

        // Use your existing utility to safely retrieve and delete keys
        Set<String> keysToDelete = safeKeys(pattern);

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("Evicted {} keys from reviewCache for trainer: {}",
                    keysToDelete.size(), trainerId);
        } else {
            log.warn("No keys found to evict in reviewCache for trainer: {}", trainerId);
        }
    }

    private Set<String> safeKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return (keys != null) ? keys : Collections.emptySet();
    }
}
