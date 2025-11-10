package com.gym.planService.Configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllMonthlyRevenueWrapperResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllPlanResponseWrapperResponseDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    @Bean
    public TypedJsonRedisSerializer<AllPlanResponseWrapperResponseDto> allPlanResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllPlanResponseWrapperResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<AllCuponCodeWrapperResponseDto> allCuponCodeWrapperResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllCuponCodeWrapperResponseDto.class);
    }

    @Bean TypedJsonRedisSerializer<AllMonthlyRevenueWrapperResponseDto> allMonthlyRevenueWrapperResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllMonthlyRevenueWrapperResponseDto.class);
    }
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            TypedJsonRedisSerializer<AllPlanResponseWrapperResponseDto> allPlanResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<AllCuponCodeWrapperResponseDto> allCuponCodeWrapperResponseDtoRedisSerializer,
            TypedJsonRedisSerializer<AllMonthlyRevenueWrapperResponseDto> allMonthlyRevenueWrapperResponseDtoRedisSerializer
    ) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        cacheConfigs.put("allPlansCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allPlanResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofDays(7)));

        cacheConfigs.put("cuponCodes",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allCuponCodeWrapperResponseDtoRedisSerializer))
                .entryTtl(Duration.ofDays(30)));

        cacheConfigs.put("allRevenue",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allMonthlyRevenueWrapperResponseDtoRedisSerializer))
                .entryTtl(Duration.ofHours(8)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
