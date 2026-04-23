package com.order_service.Order.Service.config;



import com.order_service.Order.Service.exception.OrderNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        log.error("Feign client error - method: {}, status: {}", methodKey, status);

        return switch (status) {
            case NOT_FOUND -> new OrderNotFoundException(
                    "Resource not found when calling: " + methodKey);
            case BAD_REQUEST -> new IllegalArgumentException(
                    "Bad request when calling: " + methodKey);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
