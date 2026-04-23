package com.user_service.User.Service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for User Service.
 *
 * WHY THIS EXISTS:
 *   Adding spring-security-crypto (for BCryptPasswordEncoder) pulls in
 *   spring-security-core and spring-security-web as transitive dependencies.
 *   Spring Boot's security autoconfiguration then kicks in and secures ALL
 *   endpoints with HTTP Basic auth by default — producing the 403 responses
 *   you see in the gateway logs.
 *
 * HOW IT'S FIXED:
 *   JWT authentication is handled entirely at the API Gateway level.
 *   Every request that reaches a downstream service has already been
 *   validated by the gateway, which injects X-User-Id, X-User-Email,
 *   and X-User-Role headers.
 *
 *   This config therefore:
 *     1. Disables CSRF (stateless REST API — no session)
 *     2. Disables HTTP Basic (replaced by gateway JWT filter)
 *     3. Disables form login
 *     4. Permits ALL requests — the gateway is the only auth checkpoint
 *     5. Sets session policy to STATELESS
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()  // gateway already verified the token
                );

        return http.build();
    }
}