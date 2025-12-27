package com.gym.planService.Configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Dtos.OrderDtos.Responses.MonthlyRevenueResponseDto;
import com.gym.planService.Dtos.OrderDtos.Wrappers.AllRecentTransactionsResponseWrapperDto;
import com.gym.planService.Dtos.OrderDtos.Wrappers.ReceiptResponseWrapperDto;
import com.gym.planService.Dtos.PlanDtos.Responses.MostPopularPlanIds;
import com.gym.planService.Dtos.PlanDtos.Responses.RevenueGeneratedPerPlanResponseDto;
import com.gym.planService.Dtos.PlanDtos.Responses.TotalUserResponseDto;
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

    @Bean
    public TypedJsonRedisSerializer<AllMonthlyRevenueWrapperResponseDto> allMonthlyRevenueWrapperResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllMonthlyRevenueWrapperResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<TotalUserResponseDto> totalUserResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper){
        return new TypedJsonRedisSerializer<>(redisObjectMapper,TotalUserResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer <AllRecentTransactionsResponseWrapperDto> allRecentTransactionsResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, AllRecentTransactionsResponseWrapperDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<MostPopularPlanIds> mostPopularPlanIdsRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, MostPopularPlanIds.class);
    }

    @Bean
    public TypedJsonRedisSerializer<MonthlyRevenueResponseDto> monthlyRevenueResponseDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, MonthlyRevenueResponseDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<ReceiptResponseWrapperDto> receiptResponseWrapperDtoRedisSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, ReceiptResponseWrapperDto.class);
    }

    @Bean
    public TypedJsonRedisSerializer<RevenueGeneratedPerPlanResponseDto> revenueGeneratedPerPlanResponseDtoSerializer
            (ObjectMapper redisObjectMapper) {
        return new TypedJsonRedisSerializer<>(redisObjectMapper, RevenueGeneratedPerPlanResponseDto.class);
    }
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            TypedJsonRedisSerializer<AllPlanResponseWrapperResponseDto> allPlanResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<AllCuponCodeWrapperResponseDto> allCuponCodeWrapperResponseDtoRedisSerializer,
            TypedJsonRedisSerializer<AllMonthlyRevenueWrapperResponseDto> allMonthlyRevenueWrapperResponseDtoRedisSerializer,
            TypedJsonRedisSerializer<TotalUserResponseDto> totalUserResponseDtoSerializer,
            TypedJsonRedisSerializer<AllRecentTransactionsResponseWrapperDto> allRecentTransactionsResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<MostPopularPlanIds> mostPopularPlanIdsRedisSerializer,
            TypedJsonRedisSerializer<MonthlyRevenueResponseDto> monthlyRevenueResponseDtoRedisSerializer,
            TypedJsonRedisSerializer<ReceiptResponseWrapperDto> receiptResponseWrapperDtoRedisSerializer,
            TypedJsonRedisSerializer<RevenueGeneratedPerPlanResponseDto> revenueGeneratedPerPlanResponseDtoSerializer
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

        cacheConfigs.put("totalUsers",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(totalUserResponseDtoSerializer))
                .entryTtl(Duration.ofHours(16)));

        cacheConfigs.put("recentTransactions",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.
                        fromSerializer(allRecentTransactionsResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofMinutes(20)));

        cacheConfigs.put("mostPopular",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(mostPopularPlanIdsRedisSerializer))
                .entryTtl(Duration.ofHours(2)));

        cacheConfigs.put("monthlyRevenue",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(monthlyRevenueResponseDtoRedisSerializer))
                .entryTtl(Duration.ofHours(2)));

        cacheConfigs.put("receiptCache",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(receiptResponseWrapperDtoRedisSerializer))
                .entryTtl(Duration.ofHours(4)));

        cacheConfigs.put("revenuePerPlan",defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(revenueGeneratedPerPlanResponseDtoSerializer))
                .entryTtl(Duration.ofHours(1)));

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
