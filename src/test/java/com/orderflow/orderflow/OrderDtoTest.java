package com.orderflow.orderflow;

import com.orderflow.orderflow.dto.OrderItemRequest;
import com.orderflow.orderflow.dto.OrderItemResponse;
import com.orderflow.orderflow.dto.OrderRequest;
import com.orderflow.orderflow.dto.OrderResponse;
import com.orderflow.orderflow.model.OrderStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void orderItemRequest_validInputs_shouldHaveNoViolations() {
        OrderItemRequest request = new OrderItemRequest(1L, 5);
        Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
        assertEquals(1L, request.getProductId());
        assertEquals(5, request.getQuantity());
    }

    @Test
    void orderItemRequest_nullProductId_shouldHaveViolation() {
        OrderItemRequest request = new OrderItemRequest(null, 5);
        Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Product ID cannot be null", violations.iterator().next().getMessage());
    }

    @Test
    void orderItemRequest_zeroOrNegativeProductId_shouldHaveViolation() {
        OrderItemRequest zeroRequest = new OrderItemRequest(0L, 5);
        Set<ConstraintViolation<OrderItemRequest>> zeroViolations = validator.validate(zeroRequest);
        assertEquals(1, zeroViolations.size());
        assertEquals("Product ID must be positive", zeroViolations.iterator().next().getMessage());

        OrderItemRequest negativeRequest = new OrderItemRequest(-1L, 5);
        Set<ConstraintViolation<OrderItemRequest>> negativeViolations = validator.validate(negativeRequest);
        assertEquals(1, negativeViolations.size());
        assertEquals("Product ID must be positive", negativeViolations.iterator().next().getMessage());
    }

    @Test
    void orderItemRequest_zeroOrNegativeQuantity_shouldHaveViolation() {
        OrderItemRequest request = new OrderItemRequest(1L, 0);
        Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Quantity must be at least 1", violations.iterator().next().getMessage());
    }

    @Test
    void orderRequest_validItems_shouldHaveNoViolations() {
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        OrderRequest request = new OrderRequest(List.of(item));
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void orderRequest_emptyItems_shouldHaveViolation() {
        OrderRequest request = new OrderRequest(Collections.emptyList());
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals("Order must contain at least one item", violations.iterator().next().getMessage());
    }

    @Test
    void orderResponse_shouldContainAllProperties() {
        OrderItemResponse itemResponse = new OrderItemResponse(1L, 2L, "Keyboard", 1, new BigDecimal("89.99"), new BigDecimal("89.99"));
        LocalDateTime now = LocalDateTime.now();
        OrderResponse orderResponse = new OrderResponse(10L, new BigDecimal("89.99"), OrderStatus.PENDING, now, List.of(itemResponse));

        assertEquals(10L, orderResponse.getId());
        assertEquals(new BigDecimal("89.99"), orderResponse.getTotalAmount());
        assertEquals(OrderStatus.PENDING, orderResponse.getStatus());
        assertEquals(now, orderResponse.getCreatedAt());
        assertEquals(1, orderResponse.getItems().size());
        assertEquals(1L, orderResponse.getItems().get(0).getId());
        assertEquals(2L, orderResponse.getItems().get(0).getProductId());
        assertEquals("Keyboard", orderResponse.getItems().get(0).getProductName());
        assertEquals(1, orderResponse.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("89.99"), orderResponse.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("89.99"), orderResponse.getItems().get(0).getSubtotal());
    }
}
