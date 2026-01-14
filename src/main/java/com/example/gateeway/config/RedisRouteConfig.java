/*package com.example.gateeway.config;

import org.springframework.cloud.gateway.route.RedisRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisRouteConfig {

    @Bean
    public RouteDefinitionRepository routeDefinitionRepository(ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<RouteDefinition> serializer = new Jackson2JsonRedisSerializer<>(RouteDefinition.class);
        RedisSerializationContext<String, RouteDefinition> context = RedisSerializationContext
                .<String, RouteDefinition>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        ReactiveRedisTemplate<String, RouteDefinition> template = new ReactiveRedisTemplate<>(factory, context);
        return new RedisRouteDefinitionRepository(template);
    }
}*/