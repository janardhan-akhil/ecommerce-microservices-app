package com.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Logs every inbound request and its outcome.
 *
 * Format:
 *   >>>  GET  /api/v1/products  userId=42
 *   <<<  GET  /api/v1/products  200  12ms
 *
 * Order 0: runs after auth (-2) and rate limiter (-1),
 *          before ProxyFilter (Ordered.LOWEST_PRECEDENCE).
 */
@Component
@Slf4j
public class LoggingFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name() : "?";
        String path   = exchange.getRequest().getURI().getPath();
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        long   start  = System.currentTimeMillis();

        log.info(">>> {} {} | userId={}", method, path,
                userId != null ? userId : "anonymous");

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    int  status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value() : 0;
                    long ms     = System.currentTimeMillis() - start;
                    log.info("<<< {} {} | {} | {}ms", method, path, status, ms);
                }));
    }
}