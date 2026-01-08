package com.example.gateeway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
	    http
	        .csrf().disable()
	        .authorizeExchange()
	        .pathMatchers("/auth/**").permitAll()
	        .pathMatchers("/actuator/**").permitAll()
	        .anyExchange().permitAll()    
	        .and()
	        .httpBasic().disable()
	        .formLogin().disable();

	    return http.build();
	}
}
