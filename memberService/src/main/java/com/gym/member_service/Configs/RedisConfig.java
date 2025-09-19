package com.gym.member_service.Configs;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.MemberDetailsResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.TrainerInfoResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Wrapper.AllSessionInfoResponseDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;


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
    public TypedJsonRedisSerializer<AllMemberResponseDto> memberDtoSerializer(ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllMemberResponseDto.class);
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericSerializer(ObjectMapper redisObjectMapper) {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    @Bean
    public TypedJsonRedisSerializer<MemberDetailsResponseDto> memberDetailsDtoSerializer(ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, MemberDetailsResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<TrainerInfoResponseDto> trainerInfoResponseDtoSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, TrainerInfoResponseDto.class);
    }
    @Bean
    public TypedJsonRedisSerializer<AllSessionInfoResponseDto> allSessionInfoResponseDtoSerializer(
            ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllSessionInfoResponseDto.class);
    }
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     TypedJsonRedisSerializer<AllMemberResponseDto> memberDtoSerializer,
                                     TypedJsonRedisSerializer<TrainerInfoResponseDto> trainerInfoResponseDtoSerializer,
                                     TypedJsonRedisSerializer<AllSessionInfoResponseDto> allSessionInfoResponseDtoSerializer,
                                     GenericJackson2JsonRedisSerializer genericSerializer) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        cacheConfigs.put("membersDetailsCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(memberDtoSerializer))
                .entryTtl(Duration.ofHours(2)));

        cacheConfigs.put("memberListCache", defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer))
                .entryTtl(Duration.ofMinutes(6)));

        cacheConfigs.put("memberCache", defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(memberDtoSerializer))
                .entryTtl(Duration.ofHours(3)));

        cacheConfigs.put("member'sTrainer",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(trainerInfoResponseDtoSerializer))
                .entryTtl(Duration.ofHours(3)));

        cacheConfigs.put("member'sSessionCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allSessionInfoResponseDtoSerializer))
                .entryTtl(Duration.ofHours(16)));

        cacheConfigs.put("memberCountCache",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(15)));

        cacheConfigs.put("profileImageUrl",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(18)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    public StringRedisTemplate redisTemplate (RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }
}
/*
cacheConfigs.put("memberCache", defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(memberDtoSerializer))
                .entryTtl(Duration.ofHours(3)));


 */