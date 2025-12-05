package com.gym.member_service.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomJavaEvict {
    private final StringRedisTemplate redisTemplate;

    /**
     * Helper method to manually evict cache entries based on a pattern.
     * This uses Redis's KEYS or SCAN command for pattern matching.
     * @param cacheName The name of the cache (e.g., "memberPrCache").
     * @param memberId The ID of the member.
     */
    public void evictMemberCachePattern(String cacheName, String memberId) {
        // --- FIX APPLIED HERE: Reverted separator to "::" ---
        // Based on the keys provided by the user (e.g., "memberPrCache::FSM-25M..."),
        // the RedisCacheManager is using "::" as the separator.
        String pattern = cacheName + "::" + memberId + "*";

        log.debug("Attempting manual eviction for pattern: {}", pattern);

        // Use redisTemplate.keys(pattern) to find all matching keys.
        Set<String> keysToDelete = redisTemplate.keys(pattern);

        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("Manually evicted {} keys matching pattern: {}", keysToDelete.size(), pattern);
        } else {
            log.warn("No keys found to evict for pattern: {}", pattern);
        }
    }
    public void evictMemberSessionCachePattern(String cacheName, String memberId, String type) {

        Set<String> keysToDelete = new HashSet<>();

        // MEMBER-FIRST caches (e.g., BMI)
        String memberPattern = cacheName + "::" + memberId + "*";

        // Session caches
        String upPattern   = cacheName + "::UP:" + memberId + "*";
        String pastPattern = cacheName + "::PAST:" + memberId + "*";

        log.debug("Evicting cache '{}' for member '{}' with type '{}'", cacheName, memberId, type);

        switch (type.toUpperCase()) {

            case "UP":
                keysToDelete.addAll(safeKeys(upPattern));
                break;

            case "PAST":
                keysToDelete.addAll(safeKeys(pastPattern));
                break;

            case "MEMBER":
                keysToDelete.addAll(safeKeys(memberPattern));
                break;

            case "ALL":  // Clear every related cache
                keysToDelete.addAll(safeKeys(memberPattern));
                keysToDelete.addAll(safeKeys(upPattern));
                keysToDelete.addAll(safeKeys(pastPattern));
                break;

            default:
                log.warn("Invalid cache eviction type '{}'. Allowed: UP, PAST, MEMBER, ALL", type);
                return;
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("Evicted {} keys from cache '{}' for member '{}'", keysToDelete.size(), cacheName, memberId);
        } else {
            log.warn("No keys found to evict for cache '{}' and member '{}'", cacheName, memberId);
        }
    }

    private Set<String> safeKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return (keys != null) ? keys : Collections.emptySet();
    }

}
