package com.bruin.config;

import com.bruin.serializer.FstSerializer;
import com.bruin.serializer.KryoSerializer;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Serializable;

/**
 * @description:
 * @author: xiongwenwen   2019/12/7 17:27
 */
@Configuration
public class RedisConfig {

    private FstSerializer<Object> fstSerializer;
    private KryoSerializer<Object> kryoSerializer;

    public RedisConfig(FstSerializer<Object> fstSerializer, KryoSerializer<Object> kryoSerializer) {
        this.fstSerializer = fstSerializer;
        this.kryoSerializer = kryoSerializer;
    }

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory factory){
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setDefaultSerializer(fstSerializer);
//        redisTemplate.setDefaultSerializer(kryoSerializer);

        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory){
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.string());
        redisTemplate.setConnectionFactory(factory);

        return redisTemplate;
    }
}
