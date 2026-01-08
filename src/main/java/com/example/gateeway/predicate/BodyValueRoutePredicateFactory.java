package com.example.gateeway.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

@Component
public class BodyValueRoutePredicateFactory extends AbstractRoutePredicateFactory<BodyValueRoutePredicateFactory.Config> {
    public BodyValueRoutePredicateFactory() { super(Config.class); }

    public static class Config {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            String cachedBody = exchange.getAttribute("cachedBody");
          
            return cachedBody == null || cachedBody.contains("\"value\":\"" + config.getValue() + "\"");
        };
    }
}
