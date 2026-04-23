package com.api_gateway.filter;

import com.api_gateway.config.GatewayProperties;
import com.api_gateway.utility.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private JwtUtil jwtUtil;
    private GatewayProperties gatewayProperties;
    private AuthenticationFilter filter;
    private WebFilterChain chain;

    @BeforeEach
    void setup() {
        jwtUtil = mock(JwtUtil.class);
        gatewayProperties = new GatewayProperties();
        chain = mock(WebFilterChain.class);

        filter = new AuthenticationFilter(jwtUtil, gatewayProperties);

        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    // ✅ 1. Public path should skip authentication
    @Test
    void shouldAllowPublicPath() {
        gatewayProperties.setPublicPaths(List.of("/api/v1/auth/login"));

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/auth/login").build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(any());
    }

    // ✅ 2. Product GET should be allowed without token
    @Test
    void shouldAllowProductGetWithoutAuth() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/products/1").build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(any());
    }

    // ❌ 3. Missing Authorization header → 401
    @Test
    void shouldRejectWhenAuthorizationHeaderMissing() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/orders").build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        assert exchange.getResponse().getStatusCode().value() == 401;
        verify(chain, never()).filter(any());
    }

    // ❌ 4. Invalid token → 401
    @Test
    void shouldRejectInvalidToken() {
        when(jwtUtil.isValid("invalid-token")).thenReturn(false);

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        assert exchange.getResponse().getStatusCode().value() == 401;
        verify(chain, never()).filter(any());
    }

    // ✅ 5. Valid token → should pass and mutate headers
    @Test
    void shouldAllowValidTokenAndMutateHeaders() {
        when(jwtUtil.isValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractUserId("valid-token")).thenReturn("123");
        when(jwtUtil.extractEmail("valid-token")).thenReturn("test@mail.com");
        when(jwtUtil.extractRole("valid-token")).thenReturn("USER");

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                        .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(chain).filter(argThat(ex -> {
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            String email = ex.getRequest().getHeaders().getFirst("X-User-Email");
            String role = ex.getRequest().getHeaders().getFirst("X-User-Role");

            return "123".equals(userId)
                    && "test@mail.com".equals(email)
                    && "USER".equals(role);
        }));
    }
}