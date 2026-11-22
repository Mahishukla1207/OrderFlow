package com.orderflow.orderflow.exception;

public class InactiveProductException extends RuntimeException {

    public InactiveProductException(String message) {
        super(message);
    }

    public InactiveProductException(Long productId) {
        super("Product is inactive with ID: " + productId);
    }
}
