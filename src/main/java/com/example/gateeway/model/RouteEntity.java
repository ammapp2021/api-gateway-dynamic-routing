package com.example.gateeway.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gateway_routes")
public class RouteEntity {

    @Id
    private String id;

    private String uri;

    @Column(columnDefinition = "TEXT")
    private String predicates;

    @Column(columnDefinition = "TEXT")
    private String filters;

    private boolean enabled;

    public RouteEntity() {}

    public RouteEntity(String id, String uri, String predicates, String filters, boolean enabled) {
        this.id = id;
        this.uri = uri;
        this.predicates = predicates;
        this.filters = filters;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getPredicates() { return predicates; }
    public void setPredicates(String predicates) { this.predicates = predicates; }

    public String getFilters() { return filters; }
    public void setFilters(String filters) { this.filters = filters; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
