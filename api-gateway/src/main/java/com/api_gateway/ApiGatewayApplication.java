package com.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Pure Spring Boot 4.x WebFlux API Gateway.
 *
 * No Spring Cloud annotations needed:
 *  - @EnableDiscoveryClient removed (no Eureka client)
 *  - @EnableFeignClients removed (no Feign — the gateway doesn't call services itself)
 *  - No exclude list needed (no Spring Cloud autoconfiguration on the classpath)
 *
 * All requests are reverse-proxied via WebClient to static URLs defined in
 * application.yml under gateway.routes. For Docker or Kubernetes deployments,
 * replace localhost:PORT with the container/service hostname.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}