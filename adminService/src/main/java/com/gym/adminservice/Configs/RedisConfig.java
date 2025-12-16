package com.gym.adminservice.Configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.adminservice.Dto.Responses.AllMemberRequestDtoList;
import com.gym.adminservice.Dto.Wrappers.AllMessageWrapperResponseDto;
import com.gym.adminservice.Dto.Wrappers.AllPendingRequestResponseWrapperDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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
    public TypedJsonRedisSerializer<AllPendingRequestResponseWrapperDto> allPendingRequestResponseWrapperRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllPendingRequestResponseWrapperDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<AllMessageWrapperResponseDto> allMessageWrapperResponseDtoTypedJsonRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllMessageWrapperResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<AllMemberRequestDtoList> allMemberRequestDtoListRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllMemberRequestDtoList.class);
    }

    @Bean
    public CacheManager cacheManager
            (RedisConnectionFactory factory,
             TypedJsonRedisSerializer<AllPendingRequestResponseWrapperDto> allPendingRequestResponseWrapperDtoRedisSerializer,
             TypedJsonRedisSerializer<AllMessageWrapperResponseDto> allMessageWrapperResponseDtoRedisSerializer,
             TypedJsonRedisSerializer<AllMemberRequestDtoList> allMemberRequestDtoListRedisSerializer
            ) {
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        cacheConfigs.put("PlaneCache", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(3)));

        cacheConfigs.put("pendingRequest",defaultConfig
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(allPendingRequestResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofHours(2)));

        cacheConfigs.put("memberRequests",defaultConfig
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(allMemberRequestDtoListRedisSerializer))
                .entryTtl(Duration.ofHours(4)));

        cacheConfigs.put("messagesCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(allMessageWrapperResponseDtoRedisSerializer))
                .entryTtl(Duration.ofMinutes(45)));

        cacheConfigs.put("adminMessageCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(allMessageWrapperResponseDtoRedisSerializer))
                .entryTtl(Duration.ofMinutes(15)));


        return RedisCacheManager.builder(factory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                )
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
