package com.order_service.Order.Service.exception;


import com.order_service.Order.Service.utility.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(OrderStatus current, OrderStatus requested) {
        super(String.format(
                "Cannot transition order from status '%s' to '%s'",
                current, requested
        ));
    }
}
