package com.gym.trainerService.Configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.trainerService.Dto.MemberDtos.Responses.GenericResponse;
import com.gym.trainerService.Dto.MemberDtos.Responses.SessionMatrixInfo;
import com.gym.trainerService.Dto.MemberDtos.Wrappers.AllMemberResponseWrapperDto;
import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.SpecialityResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.ClientMatrixInfo;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerDashBoardInfoResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllPublicTrainerInfoResponseWrapperDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.RatingMatrixInfo;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import com.gym.trainerService.Models.Trainer;
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

/**
 * Configuration class responsible for setting up Redis-based caching for the Trainer Service.
 * <p>
 * This configuration establishes custom {@link CacheManager} behavior, key/value serializers,
 * and per-cache time-to-live (TTL) policies across multiple domain layers.
 * It enables fine-grained Redis caching through {@link EnableCaching} annotation integrated
 * with the Spring Cache abstraction.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Registers type-safe custom serializers using {@link TypedJsonRedisSerializer}.</li>
 *   <li>Provides per-cache TTL settings to maintain data freshness.</li>
 *   <li>Configures a shared {@link ObjectMapper} for consistent JSON handling.</li>
 *   <li>Defines a {@link StringRedisTemplate} for simple Redis string operations.</li>
 * </ul>
 *
 * <p><b>Applicable Caches:</b></p>
 * <ul>
 *   <li>AllTrainerCache — TTL 6 hours</li>
 *   <li>trainerCache — TTL 18 hours</li>
 *   <li>reviewCache — TTL 16 hours</li>
 *   <li>AllMemberListCache — TTL 6 hours</li>
 *   <li>AllSessionCache — TTL 16 hours</li>
 *   <li>profileImageCache — TTL 2 days</li>
 * </ul>
 *
 * @author Arpan
 * @since  1.0
 */

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configures the base {@link ObjectMapper} used for all Redis serializations.
     * <p>
     * This mapper is customized to:
     * <ul>
     *   <li>Support Java 8 date/time serialization via {@link JavaTimeModule}</li>
     *   <li>Write dates in ISO-8601 instead of timestamps</li>
     *   <li>Ignore unknown JSON properties during deserialization</li>
     * </ul>
     *
     * @return a customized and reusable {@link ObjectMapper} instance
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    /**
     * Defines a custom serializer for caching trainer {@link Trainer} by their id
     *
     * @param redisObjectMapper the shared {@link ObjectMapper} used for JSON serialization
     * @return serializer for {@link Trainer}
     * */
    @Bean
    public TypedJsonRedisSerializer<Trainer> trainerTypedJsonRedisSerializer(ObjectMapper redisObjectMapper) {
        return  new TypedJsonRedisSerializer<>(redisObjectMapper,Trainer.class);
    }

    /**
     * Defines a custom Redis serializer for caching trainer lists.
     *
     * @param redisObjectMapper the shared {@link ObjectMapper} used for JSON serialization
     * @return serializer for {@code AllTrainerResponseDtoWrapper}
     */
    @Bean
    public TypedJsonRedisSerializer<AllTrainerResponseDtoWrapper> allTrainerResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllTrainerResponseDtoWrapper.class );
    }

    /**
     * Defines a custom Redis serializer for individual trainer responses.
     *
     * @param redisObjectMapper the shared {@link ObjectMapper}
     * @return serializer for {@code TrainerResponseDto}
     */
    @Bean
    public TypedJsonRedisSerializer<TrainerResponseDto> trainerResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, TrainerResponseDto.class);
    }

    /**
     * Defines a custom Redis serializer for trainer review response wrappers.
     *
     * @param redisObjectMapper the shared {@link ObjectMapper}
     * @return serializer for {@code AllReviewResponseWrapperDto}
     */
    @Bean
    public TypedJsonRedisSerializer<AllReviewResponseWrapperDto> allReviewResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllReviewResponseWrapperDto.class);
    }

    /**
     * Defines a custom Redis serializer for member list response wrappers.
     *
     * @param redisObjectMapper the shared {@link ObjectMapper}
     * @return serializer for {@code AllMemberResponseWrapperDto}
     */
    @Bean
    public TypedJsonRedisSerializer<AllMemberResponseWrapperDto> allMemberResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,AllMemberResponseWrapperDto.class);
    }

    /**
     * Defines a custom Redis serializer for all session-related caches.
     *
     * @param redisObjectMapper the shared {@link ObjectMapper}
     * @return serializer for {@code AllSessionsWrapperDto}
     */
    @Bean
    public TypedJsonRedisSerializer<AllSessionsWrapperDto> allSessionsWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllSessionsWrapperDto.class);
    }

    /**
     * Defines a custom Redis Serializer for all trainer's basic info
     * which will be shown in the home page
     * @param redisObjectMapper  the shared {@link ObjectMapper}
     * @return serializer for {@code AllPublicTrainerInfoResponseWrapperDto}
     * */
    @Bean
    public TypedJsonRedisSerializer<AllPublicTrainerInfoResponseWrapperDto> allPublicTrainerInfoResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllPublicTrainerInfoResponseWrapperDto.class);
    }
    /**
     * Defines a custom Redis Serialize for all Specialites written in the
     * {@link com.gym.trainerService.Enums.TrainerSpeciality}
     * @param redisObjectMapper the shared {@link ObjectMapper}
     * @return serializer for {@code SpecialityResponseDto}
     */
    @Bean
    public TypedJsonRedisSerializer<SpecialityResponseDto> specialityResponseDtoTypedJsonRedisSerializer
    (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper,SpecialityResponseDto.class);
    }

    /**
     * Defines a fallback generic serializer for miscellaneous or untyped caches (e.g., profile images).
     *
     * @param redisObjectMapper the shared {@link ObjectMapper}
     * @return a general-purpose Jackson-based Redis serializer
     */
    @Bean
    public GenericJackson2JsonRedisSerializer genericSerializer(ObjectMapper redisObjectMapper) {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }


    @Bean
    public TypedJsonRedisSerializer<ClientMatrixInfo> clientMatrixInfoTypedJsonRedisSerializer
            (ObjectMapper objectMapper) {
        return new TypedJsonRedisSerializer<>(objectMapper, ClientMatrixInfo.class);
    }

    @Bean
    public TypedJsonRedisSerializer<SessionMatrixInfo> sessionMatrixInfoTypedJsonRedisSerializer
            (ObjectMapper objectMapper) {
        return new TypedJsonRedisSerializer<>(objectMapper,SessionMatrixInfo.class);
    }

    @Bean
    public TypedJsonRedisSerializer<RatingMatrixInfo> ratingMatrixInfoTypedJsonRedisSerializer
            (ObjectMapper objectMapper) {
        return  new TypedJsonRedisSerializer<>(objectMapper,RatingMatrixInfo.class);
    }

    @Bean
    public TypedJsonRedisSerializer<TrainerDashBoardInfoResponseDto> trainerDashBoardInfoResponseDtoRedisSerializer
            (ObjectMapper objectMapper) {
        return new TypedJsonRedisSerializer<>(objectMapper,TrainerDashBoardInfoResponseDto.class);
    }
    /**
     * Configures and provides the central {@link CacheManager} that manages all Redis caches.
     * <p>
     * For each cache region, a distinct TTL and serializer mapping is applied to optimize expiration times.
     * </p>
     *
     * @param connectionFactory the active Redis connection factory
     * @param allTrainerResponseDtoRedisSerializer trainer list cache serializer
     * @param trainerResponseDtoRedisSerializer trainer details cache serializer
     * @param allReviewResponseWrapperDtoRedisSerializer review cache serializer
     * @param allMemberResponseWrapperDtoRedisSerializer member list cache serializer
     * @param allSessionsWrapperDtoRedisSerializer session list cache serializer
     * @param genericRedisSerializer fallback serializer for untyped use cases
     * @param allPublicTrainerInfoResponseWrapperDtoRedisSerializer trainer's basic info list cache serializer
     * @param specialityResponseDtoTypedJsonRedisSerializer for all trainer's speciality and all possible speciality
     * @param trainerTypedJsonRedisSerializer serializer for trainer entity
     * @param clientMatrixInfoTypedJsonRedisSerializer serializer for trainer's client count
     * @param sessionMatrixInfoTypedJsonRedisSerializer store session matrix of trainer
     * @param ratingMatrixInfoTypedJsonRedisSerializer  store review matrix of trainer
     * @return configured {@link RedisCacheManager} with specific TTLs and serializers
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            TypedJsonRedisSerializer<AllTrainerResponseDtoWrapper> allTrainerResponseDtoRedisSerializer,
            TypedJsonRedisSerializer<TrainerResponseDto> trainerResponseDtoRedisSerializer,
            TypedJsonRedisSerializer<AllReviewResponseWrapperDto> allReviewResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<AllMemberResponseWrapperDto> allMemberResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<AllSessionsWrapperDto> allSessionsWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<AllPublicTrainerInfoResponseWrapperDto> allPublicTrainerInfoResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<Trainer> trainerTypedJsonRedisSerializer,
            TypedJsonRedisSerializer<SpecialityResponseDto> specialityResponseDtoTypedJsonRedisSerializer,
            TypedJsonRedisSerializer<ClientMatrixInfo> clientMatrixInfoTypedJsonRedisSerializer,
            TypedJsonRedisSerializer<SessionMatrixInfo>sessionMatrixInfoTypedJsonRedisSerializer,
            TypedJsonRedisSerializer<RatingMatrixInfo> ratingMatrixInfoTypedJsonRedisSerializer,
            TypedJsonRedisSerializer<TrainerDashBoardInfoResponseDto> trainerDashBoardInfoResponseDtoRedisSerializer,
            GenericJackson2JsonRedisSerializer genericRedisSerializer) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        cacheConfigs.put("trainer",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.
                        fromSerializer(trainerTypedJsonRedisSerializer))
                .entryTtl(Duration.ofHours(4)));

        cacheConfigs.put("about",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericRedisSerializer))
                .entryTtl(Duration.ofHours(6)));

        cacheConfigs.put("speciality",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(specialityResponseDtoTypedJsonRedisSerializer))
                .entryTtl(Duration.ofHours(6)));

        cacheConfigs.put("allSpeciality",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(specialityResponseDtoTypedJsonRedisSerializer))
                .entryTtl(Duration.ofDays(7)));

        cacheConfigs.put("trainerBasic",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(allPublicTrainerInfoResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofHours(12)));

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

        cacheConfigs.put("ClientMatrix",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(clientMatrixInfoTypedJsonRedisSerializer))
                .entryTtl(Duration.ofHours(4)));

        cacheConfigs.put("sessionMatrix",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(sessionMatrixInfoTypedJsonRedisSerializer))
                .entryTtl(Duration.ofHours(4)));

        cacheConfigs.put("ratingMatrix",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(ratingMatrixInfoTypedJsonRedisSerializer))
                .entryTtl(Duration.ofMinutes(30)));

        cacheConfigs.put("DashboardInfo",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(trainerDashBoardInfoResponseDtoRedisSerializer))
                .entryTtl(Duration.ofHours(4)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    /**
     * Defines a {@link StringRedisTemplate} bean used for straightforward key-value
     * string handling in Redis without object serialization.
     *
     * @param connectionFactory the active Redis connection factory
     * @return the {@link StringRedisTemplate} configured for this service
     */
    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }
}
