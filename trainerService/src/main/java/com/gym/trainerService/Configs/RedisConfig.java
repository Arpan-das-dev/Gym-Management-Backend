package com.gym.trainerService.Configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.trainerService.Dto.MemberDtos.Wrappers.AllMemberResponseWrapperDto;
import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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
    public TypedJsonRedisSerializer<AllTrainerResponseDtoWrapper> allTrainerResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllTrainerResponseDtoWrapper.class );
    }

    @Bean
    public TypedJsonRedisSerializer<TrainerResponseDto> trainerResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, TrainerResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<AllReviewResponseWrapperDto> allReviewResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllReviewResponseWrapperDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<AllMemberResponseWrapperDto> allMemberResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllMemberResponseWrapperDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<AllSessionsWrapperDto> allSessionsWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllSessionsWrapperDto.class);
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericSerializer(ObjectMapper redisObjectMapper) {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }


    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     TypedJsonRedisSerializer<AllTrainerResponseDtoWrapper> allTrainerResponseDtoRedisSerializer,
                                     TypedJsonRedisSerializer<TrainerResponseDto> trainerResponseDtoRedisSerializer,
                                     TypedJsonRedisSerializer<AllReviewResponseWrapperDto> allReviewResponseWrapperDtoRedisSerializer,
                                     TypedJsonRedisSerializer<AllMemberResponseWrapperDto> allMemberResponseWrapperDtoRedisSerializer,
                                     TypedJsonRedisSerializer<AllSessionsWrapperDto> allSessionsWrapperDtoRedisSerializer,
                                     GenericJackson2JsonRedisSerializer genericRedisSerializer)
    {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        cacheConfigs.put("AllTrainerCache", defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allTrainerResponseDtoRedisSerializer))
                .entryTtl(Duration.ofHours(6)));

        cacheConfigs.put("trainerCache", defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(trainerResponseDtoRedisSerializer))
                .entryTtl(Duration.ofHours(18)));

        cacheConfigs.put("reviewCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allReviewResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofHours(16)));

        cacheConfigs.put("AllMemberListCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allMemberResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofHours(6)));

        cacheConfigs.put("AllSessionCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allSessionsWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofHours(16)));

        cacheConfigs.put("profileImageCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(genericRedisSerializer))
                .entryTtl(Duration.ofDays(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

}
