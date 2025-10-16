package com.gym.trainerService.Configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

/**
 * Custom Redis serializer and deserializer for handling typed JSON objects.
 * <p>
 * This implementation uses the Jackson {@link ObjectMapper} to serialize and deserialize
 * Redis data into strongly typed Java objects, maintaining type safety while working
 * with Redis key-value storage. It is particularly helpful when storing domain objects
 * as JSON in Redis without type erasure at runtime.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper();
 * RedisSerializer<TrainerStatus> serializer =
 *         new TypedJsonRedisSerializer<>(mapper, TrainerStatus.class);
 * }</pre>
 *
 * <p><b>Advantages:</b></p>
 * <ul>
 *   <li>Preserves type information for deserialization.</li>
 *   <li>Provides safer handling of JSON payloads than default string serializers.</li>
 *   <li>Ensures interoperability between Java and Redis JSON storage.</li>
 * </ul>
 *
 * @param <T> the target object type to serialize or deserialize
 * @author Arpan
 * @since 1.0
 */
public class TypedJsonRedisSerializer <T> implements RedisSerializer<T> {

    private final ObjectMapper mapper;
    private final JavaType javaType;

    /**
     * Constructs a new typed JSON Redis serializer for the specified target class.
     *
     * @param mapper      the Jackson {@link ObjectMapper} used for JSON processing
     * @param targetClass the class type of the serialized and deserialized object
     */
    public TypedJsonRedisSerializer(ObjectMapper mapper, Class<T> targetClass) {
        this.mapper = mapper;
        this.javaType = mapper.getTypeFactory().constructType(targetClass);
    }

    /**
     * Serializes the provided Java object into a JSON byte array suitable for storage in Redis.
     *
     * @param value the object to serialize; may be {@code null}
     * @return a byte array containing the JSON representation of the object,
     *         or an empty byte array if the object is {@code null}
     *
     * @throws SerializationException if a JSON processing error occurs during serialization
     */
    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) return new byte[0];
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Serialize error", e);
        }
    }

    /**
     * Deserializes a JSON byte array from Redis into a strongly typed Java object.
     *
     * @param bytes the byte array containing JSON data
     * @return the deserialized object instance, or {@code null} if input is empty or null
     *
     * @throws SerializationException if a JSON parsing or type conversion error occurs
     */
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
