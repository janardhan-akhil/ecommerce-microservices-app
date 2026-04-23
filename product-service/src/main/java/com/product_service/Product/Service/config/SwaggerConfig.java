package com.product_service.Product.Service.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Product Service API")
                        .version("1.0")
                        .description("API documentation for Product Service"));
    }
}
