package com.bruin.serializer;

import org.nustaq.serialization.FSTConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: xiongwenwen   2019/12/7 18:08
 */
@Component
public class FstSerializer<T> implements RedisSerializer<T> {

    private static FSTConfiguration configuration = FSTConfiguration.createStructConfiguration();
    private Class<T> clazz;

    public FstSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
        configuration.registerClass(clazz);
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return configuration.asByteArray(t);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return (T) configuration.asObject(bytes);
    }
}
