package com.gym.authservice.Config.RateLimmiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

//@Slf4j
//@Service
//public class TokenBucketRateLimiter {
//    private final Double BURST_CAPACITY;
//    private final Double REFILL_RATE;
//    private final ReactiveRedisTemplate<String , String > redisTemplate;
//
//    public TokenBucketRateLimiter(@Value("${rateLimit.capacity}") Double BURST_CAPACITY,
//                                  @Value("${rateLimit.fillRate}") Double REFILL_RATE,
//                                  @Qualifier("rateLimitRedisTemplate")  ReactiveRedisTemplate<String, String> redisTemplate)
//    {
//        this.BURST_CAPACITY = BURST_CAPACITY;
//        this.REFILL_RATE = REFILL_RATE;
//        this.redisTemplate = redisTemplate;
//    }
//
//    public Mono<Boolean> allowRequest (String key) {
//        String redisKey = "rate:limit:"+key;
//        long now = System.currentTimeMillis();
//        return redisTemplate.opsForHash()
//                .entries(redisKey)
//                .collectMap(
//                        e-> e.getKey().toString(),
//                e-> e.getValue().toString())
//                .defaultIfEmpty(Map.of())
//                .flatMap(state-> {
//                    double tokens = state.containsKey("tokens") ?
//                            Double.parseDouble(state.get("tokens"))
//                            : BURST_CAPACITY;
//                    long latestRefills = state.containsKey("latestRefills") ?
//                            Long.parseLong(state.get("latestRefills"))
//                            : now;
//
//                    double refillTokens = ((now-latestRefills) / 1000.0) * REFILL_RATE;
//                    tokens = Math.min(BURST_CAPACITY,tokens+refillTokens);
//                    if(tokens<1) {
//                        return Mono.just(false);
//                    }
//
//                    double consumedToken = tokens -1;
//                    Map<String , String > updateState = Map.of(
//                            "tokens",String.valueOf(consumedToken),
//                            "latestRefills", String.valueOf(now)
//                    );
//                    return redisTemplate.opsForHash()
//                            .putAll(redisKey,updateState)
//                            .then(redisTemplate.expire(redisKey, Duration.ofMinutes(10)))
//                            .thenReturn(true);
//                });
//    }
//}
