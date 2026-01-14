package com.example.gateeway.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileRouteRepository implements RouteDefinitionRepository {

    private static final String FILE_PATH = "D:/my-configs/routes.json.txt";

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, RouteDefinition> routes = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists() && file.length() > 0) {
                List<RouteDefinition> list =
                        mapper.readValue(file, new TypeReference<List<RouteDefinition>>() {});
                list.forEach(r -> routes.put(r.getId(), r));
                System.out.println("Routes loaded from external file");
            }
        } catch (Exception e) {
            System.err.println("Route load error: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            mapper.writeValue(new File(FILE_PATH),
                    new ArrayList<>(routes.values()));
        } catch (Exception e) {
            System.err.println("Route save error: " + e.getMessage());
        }
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.doOnNext(r -> {
            routes.put(r.getId(), r);
            persist();
        }).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.doOnNext(id -> {
            routes.remove(id);
            persist();
        }).then();
    }
}
