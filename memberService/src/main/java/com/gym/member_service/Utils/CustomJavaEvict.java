package com.gym.member_service.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
}
