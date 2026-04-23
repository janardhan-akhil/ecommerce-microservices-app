package com.cart_service.Cart.Service.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Pure-Jackson implementation of RedisSerializer.
 *
 * Replaces Spring's GenericJackson2JsonRedisSerializer / Jackson2JsonRedisSerializer,
 * both of which are deprecated or undergoing changes in Spring Data Redis 4.x.
 *
 * Because we own this class, it will never be deprecated by a framework update.
 */
public class JacksonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper mapper;

    public JacksonRedisSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return mapper.writeValueAsBytes(value);
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize object to JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize bytes to object: " + ex.getMessage(), ex);
        }
    }
}
