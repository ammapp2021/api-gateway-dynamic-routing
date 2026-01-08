/*package com.example.gateeway.config;

import com.example.gateeway.dto.ValueRequest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("route_value_1", r -> r
                        .path("/route")
                        .and()
                        .method("POST")
                        .and()
                        .readBody(ValueRequest.class, body -> body.getValue() == 1)
                        .filters(f -> f
                                .rewritePath("/route", "/posts")
                                .filter((exchange, chain) -> forwardAuthHeader(exchange, chain))
                        )
                        .uri("https://jsonplaceholder.typicode.com"))

                .route("route_value_2", r -> r
                        .path("/route")
                        .and()
                        .method("POST")
                        .and()
                        .readBody(ValueRequest.class, body -> body.getValue() == 2)
                        .filters(f -> f
                                .rewritePath("/route", "/post")
                                .filter((exchange, chain) -> forwardAuthHeader(exchange, chain))
                        )
                        .uri("https://httpbin.org"))

                .route("route_value_3", r -> r
                        .path("/route")
                        .and()
                        .method("POST")
                        .and()
                        .readBody(ValueRequest.class, body -> body.getValue() == 3)
                        .filters(f -> f
                                .rewritePath("/route", "/products/add")
                                .filter((exchange, chain) -> forwardAuthHeader(exchange, chain))
                        )
                        .uri("https://dummyjson.com"))

                .build();
    }

    private Mono<Void> forwardAuthHeader(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            exchange = exchange.mutate()
                    .request(r -> r.header(HttpHeaders.AUTHORIZATION, authHeader))
                    .build();
        }
        return chain.filter(exchange);
    }
}
*/