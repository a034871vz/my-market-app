package ru.yandex.practicum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.entity.Item;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, Item> itemRedisTemplate(
            ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {

        Jackson2JsonRedisSerializer<Item> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Item.class);

        RedisSerializationContext<String, Item> context = RedisSerializationContext
                .<String, Item>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<Item>> listRedisTemplate(
            ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {

        Jackson2JsonRedisSerializer<List<Item>> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));

        RedisSerializationContext<String, List<Item>> context = RedisSerializationContext
                .<String, List<Item>>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Long> longRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<Long> serializer =
                new Jackson2JsonRedisSerializer<>(Long.class);

        RedisSerializationContext<String, Long> context = RedisSerializationContext
                .<String, Long>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}