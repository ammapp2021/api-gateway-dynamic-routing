package com.example.gateeway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


//@EnableDiscoveryClient
@SpringBootApplication
public class GateewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GateewayApplication.class, args);
	}

}
