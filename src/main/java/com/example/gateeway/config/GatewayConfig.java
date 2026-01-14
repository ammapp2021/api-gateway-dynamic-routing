/*package com.example.gateeway.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GatewayConfig {

    @Bean("routeDefinitionRepository")
    @Primary
    @RefreshScope
    public RouteDefinitionRepository customRouteRepository(FileRouteRepository myFileRepository) {
        return myFileRepository;
    }
}*/