package com.api_gateway.filter;

import com.api_gateway.config.GatewayProperties;
import com.api_gateway.config.RouteDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProxyFilter implements WebFilter, Ordered {

    private final WebClient webClient;
    private final GatewayProperties gatewayProperties;

    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "transfer-encoding",
            "te", "trailers", "upgrade",
            "proxy-authorization", "proxy-authenticate"
    );

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest  request  = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path  = request.getURI().getPath();
        String query = request.getURI().getQuery();

        // ── PERMANENT FIX: skip proxying actuator paths ───────────────────────────────
        // /actuator/health, /actuator/info etc. are served by the gateway's own
        // Spring Boot actuator — they must NOT be proxied to a downstream service.
        // Passing to chain.filter() hands control to Spring Boot's actuator handler.
        if (path.startsWith("/actuator") ) {
            return chain.filter(exchange);
        }

        // ── Find matching downstream route ────────────────────────────────────────────
        Optional<RouteDefinition> matched = gatewayProperties.getRoutes().stream()
                .filter(r -> path.startsWith(r.getPath()))
                .findFirst();

        if (matched.isEmpty()) {
            log.warn("No route found for path: {}", path);
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.setComplete();
        }

        String targetUrl = matched.get().getUrl() + path
                + (query != null && !query.isBlank() ? "?" + query : "");

        log.debug("Proxying {} {} → {}", request.getMethod(), path, targetUrl);

        return webClient
                .method(request.getMethod())
                .uri(URI.create(targetUrl))
                .headers(forwardHeaders -> {
                    request.getHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                            forwardHeaders.addAll(name, values);
                        }
                    });
                })
                .body(request.getBody(), DataBuffer.class)
                .exchangeToMono(clientResponse -> {
                    response.setStatusCode(clientResponse.statusCode());
                    clientResponse.headers().asHttpHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                            response.getHeaders().addAll(name, values);
                        }
                    });
                    return response.writeWith(
                            clientResponse.bodyToFlux(DataBuffer.class));
                })
                .onErrorResume(ex -> {
                    log.error("Proxy error for {} {}: {}",
                            request.getMethod(), targetUrl, ex.getMessage());
                    if (!response.isCommitted()) {
                        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        return response.setComplete();
                    }
                    return Mono.empty();
                });
    }
}