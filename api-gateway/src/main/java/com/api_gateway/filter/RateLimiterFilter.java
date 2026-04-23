package com.api_gateway.filter;


import com.api_gateway.config.GatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * Per-user / per-IP rate limiter backed by Redis.
 *
 * Strategy: sliding minute window.
 *   Key:   rate:<userId-or-ip>:<epoch-minute>
 *   Value: request count for that minute
 *   TTL:   60 seconds (auto-expires, so Redis never accumulates stale keys)
 *
 * Authenticated requests are keyed by X-User-Id (injected by AuthenticationFilter).
 * Anonymous requests are keyed by the client IP address.
 *
 * Order -1: runs after AuthenticationFilter (-2), before LoggingFilter (0).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterFilter implements WebFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;
    private final GatewayProperties gatewayProperties;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String redisKey  = buildKey(exchange);
           int limit     = gatewayProperties.getRateLimitPerMinute();

        return reactiveStringRedisTemplate.opsForValue()
                .increment(redisKey)
                .flatMap(count -> {
                    if (count == 1L) {
                        // First request this minute — set TTL so key auto-expires
                        return reactiveStringRedisTemplate
                                .expire(redisKey, Duration.ofMinutes(1))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    long remaining = Math.max(0, limit - count);

                    exchange.getResponse().getHeaders()
                            .add("X-RateLimit-Limit",     String.valueOf(limit));
                    exchange.getResponse().getHeaders()
                            .add("X-RateLimit-Remaining", String.valueOf(remaining));

                    if (count > limit) {
                        log.warn("Rate limit exceeded — key={} count={} limit={}",
                                redisKey, count, limit);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders()
                                .add("Retry-After", "60");
                        return exchange.getResponse().setComplete();
                    }

                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> {
                    // If Redis is unavailable, fail open — don't block legitimate traffic
                    log.error("Rate limiter Redis error (failing open): {}", ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    private String buildKey(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String subject = (userId != null && !userId.isBlank())
                ? "user:" + userId
                : "ip:" + Objects.requireNonNull(
                        exchange.getRequest().getRemoteAddress())
                .getAddress().getHostAddress();

        // epoch-minute bucket: changes every 60 seconds
        long minuteBucket = System.currentTimeMillis() / 60_000;
        return "rate:" + subject + ":" + minuteBucket;
    }
}
