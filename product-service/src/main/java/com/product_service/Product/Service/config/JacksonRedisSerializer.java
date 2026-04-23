package com.product_service.Product.Service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.SerializationException;
import org.springframework.data.redis.serializer.RedisSerializer;

public class JacksonRedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper mapper;
    private final Class<T> type;

    public JacksonRedisSerializer(ObjectMapper mapper, Class<T> type) {
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    public byte[] serialize(T value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new SerializationException("Serialization error", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) {
        try {
            return mapper.readValue(bytes, type); // ✅ FIXED
        } catch (Exception e) {
            throw new SerializationException("Deserialization error", e);
        }
    }
}