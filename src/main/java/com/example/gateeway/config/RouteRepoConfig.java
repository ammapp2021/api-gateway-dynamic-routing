package com.example.gateeway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Primary;

@Configuration
public class RouteRepoConfig {

    @Bean
    //@Primary
    public RouteDefinitionRepository routeDefinitionRepository() {

        return new FileRouteRepository();
    }
}
