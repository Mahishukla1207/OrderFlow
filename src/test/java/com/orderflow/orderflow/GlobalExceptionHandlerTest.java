package com.orderflow.orderflow;

import com.orderflow.orderflow.dto.ErrorResponse;
import com.orderflow.orderflow.exception.GlobalExceptionHandler;
import com.orderflow.orderflow.exception.InactiveProductException;
import com.orderflow.orderflow.exception.InsufficientStockException;
import com.orderflow.orderflow.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleProductNotFoundException_shouldReturn404() {
        ProductNotFoundException ex = new ProductNotFoundException(42L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleProductNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Product Not Found", response.getBody().getError());
        assertEquals("Product not found with ID: 42", response.getBody().getMessages().get(0));
    }

    @Test
    void handleInsufficientStockException_shouldReturn400() {
        InsufficientStockException ex = new InsufficientStockException("Insufficient stock for product: Laptop");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientStockException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Insufficient Stock", response.getBody().getError());
        assertEquals("Insufficient stock for product: Laptop", response.getBody().getMessages().get(0));
    }

    @Test
    void handleInactiveProductException_shouldReturn400() {
        InactiveProductException ex = new InactiveProductException(15L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInactiveProductException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Inactive Product", response.getBody().getError());
        assertEquals("Product is inactive with ID: 15", response.getBody().getMessages().get(0));
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid parameter", response.getBody().getMessages().get(0));
    }
}
