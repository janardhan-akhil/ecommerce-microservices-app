package com.product_service.Product.Service.payload;

import lombok.Data;

import java.util.Map;

@Data
public class ValidationErrorResponse {

    public Map<String, String> errors;
}
