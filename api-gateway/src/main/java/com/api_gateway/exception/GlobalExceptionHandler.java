package com.api_gateway.exception;

import com.api_gateway.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;



/**
 * Catches any exception that escapes the filter chain and returns
 * structured JSON instead of Spring's default HTML error page.
 *
 * Spring Boot 4.x: implements WebExceptionHandler (org.springframework.web.server)
 * NOT ErrorWebExceptionHandler (org.springframework.boot.web.reactive.error — removed).
 *
 * Order -2: runs before Spring Boot's default error handler.
 */
@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements WebExceptionHandler {

    private static final JsonMapper MAPPER =
            JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Unhandled gateway exception on {}: {}",
                exchange.getRequest().getURI(), ex.getMessage());

        HttpStatus status  = resolveStatus(ex);
        String     message = resolveMessage(ex);
        String     path    = exchange.getRequest().getURI().getPath();

        ErrorResponse body = ErrorResponse.of(
                status.value(), status.getReasonPhrase(), message, path);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Jackson 3.x: writeValueAsBytes() is unchecked — no try/catch required
        byte[]     bytes  = null;
        try {
            bytes = MAPPER.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex) {
        if (ex instanceof ResponseStatusException rse && rse.getReason() != null) {
            return rse.getReason();
        }
        return "An unexpected gateway error occurred.";
    }
}