package com.gym.trainerService.Configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

public class TypedJsonRedisSerializer <T> implements RedisSerializer<T> {
    private final ObjectMapper mapper;
    private final JavaType javaType;

    public TypedJsonRedisSerializer(ObjectMapper mapper, Class<T> targetClass) {
        this.mapper = mapper;
        this.javaType = mapper.getTypeFactory().constructType(targetClass);
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) return new byte[0];
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Serialize error", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return mapper.readValue(bytes, javaType);
        } catch (IOException e) {
            throw new SerializationException("Deserialize error", e);
        }
    }
}
