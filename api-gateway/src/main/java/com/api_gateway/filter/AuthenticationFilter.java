package com.api_gateway.filter;

import com.api_gateway.config.GatewayProperties;
import com.api_gateway.utility.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements WebFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final GatewayProperties gatewayProperties;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final JsonMapper MAPPER =
            JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String     path   = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // ── 1. Actuator — always bypass, handled by gateway itself, never proxied ──
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        // ── 2. Configured public paths from application.yml ───────────────────────
        if (isConfiguredPublicPath(path)) {
            log.debug("Public route — skipping auth: {}", path);
            return chain.filter(exchange);
        }

        // ── 3. GET on product catalogue is public ─────────────────────────────────
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/v1/products")) {
            log.debug("Public product GET — skipping auth: {}", path);
            return chain.filter(exchange);
        }

        // ── 4. All other routes require a valid JWT ───────────────────────────────
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header for protected route: {}", path);
            try {
                return writeError(exchange, HttpStatus.UNAUTHORIZED,
                        "Missing or malformed Authorization header. " +
                                "Include: Authorization: Bearer <token>");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            log.warn("Invalid or expired JWT for path: {}", path);
            try {
                return writeError(exchange, HttpStatus.UNAUTHORIZED,
                        "Invalid or expired token. Please login again.");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // ── 5. Inject user context headers for downstream services ────────────────
        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id",    jwtUtil.extractUserId(token))
                .header("X-User-Email", jwtUtil.extractEmail(token))
                .header("X-User-Role",  jwtUtil.extractRole(token))
                .build();

        log.debug("Authenticated userId={} -> {}",
                jwtUtil.extractUserId(token), path);
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return -2;
    }

    private boolean isConfiguredPublicPath(String path) {
        for (String publicPath : gatewayProperties.getPublicPaths()) {
            if (PATH_MATCHER.match(publicPath, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> writeError(ServerWebExchange exchange,
                                  HttpStatus status, String message) throws JsonProcessingException {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        body.put("path",      exchange.getRequest().getURI().getPath());
        body.put("timestamp", LocalDateTime.now().toString());

        byte[]     bytes  = MAPPER.writeValueAsBytes(body);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}