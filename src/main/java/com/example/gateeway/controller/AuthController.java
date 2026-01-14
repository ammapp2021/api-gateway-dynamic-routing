package com.example.gateeway.controller;

import com.example.gateeway.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestParam String username, @RequestParam String password) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = jwtUtil.generateToken(username);

            return Mono.just(ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(token));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
        }
    }

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("JWT works!");
    }
}