package com.orderflow.orderflow.service;

import com.orderflow.orderflow.dto.OrderItemRequest;
import com.orderflow.orderflow.dto.OrderItemResponse;
import com.orderflow.orderflow.dto.OrderRequest;
import com.orderflow.orderflow.dto.OrderResponse;
import com.orderflow.orderflow.exception.InactiveProductException;
import com.orderflow.orderflow.exception.InsufficientStockException;
import com.orderflow.orderflow.exception.ProductNotFoundException;
import com.orderflow.orderflow.model.Order;
import com.orderflow.orderflow.model.OrderItem;
import com.orderflow.orderflow.model.OrderStatus;
import com.orderflow.orderflow.model.Product;
import com.orderflow.orderflow.repository.OrderRepository;
import com.orderflow.orderflow.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        validateOrderRequest(request);

        // Sort items by product ID in ascending order to prevent deadlocks under concurrent multi-product orders
        List<OrderItemRequest> sortedItemRequests = request.getItems().stream()
                .sorted(Comparator.comparing(OrderItemRequest::getProductId))
                .toList();

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order(BigDecimal.ZERO, OrderStatus.PENDING, LocalDateTime.now());

        for (OrderItemRequest itemRequest : sortedItemRequests) {
            Long productId = itemRequest.getProductId();
            Integer requestedQuantity = itemRequest.getQuantity();

            // Retrieve product with pessimistic write lock in ascending product ID order
            Product product = productRepository.findByIdWithPessimisticLock(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new InactiveProductException(productId);
            }

            int availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (availableStock < requestedQuantity) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName()
                                + " (ID: " + product.getId() + "). Requested: " + requestedQuantity
                                + ", Available: " + availableStock
                );
            }

            // Decrease product stock by requested quantity
            product.setStockQuantity(availableStock - requestedQuantity);
            productRepository.save(product);

            // Compute unit price and subtotal strictly from persisted Product entity
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
            totalAmount = totalAmount.add(subtotal);

            // Associate OrderItem with Order via helper method
            OrderItem orderItem = new OrderItem(product, requestedQuantity, unitPrice, subtotal);
            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return orderRepository.findByIdWithItems(id).map(this::mapToOrderResponse);
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request must not be null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Duplicate policy and ID/quantity validation
        Set<Long> seenProductIds = new HashSet<>();
        for (OrderItemRequest item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId() <= 0) {
                throw new IllegalArgumentException("Product ID must be a positive number");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1 for product ID: " + item.getProductId());
            }
            if (!seenProductIds.add(item.getProductId())) {
                throw new IllegalArgumentException("Duplicate product ID found in order request: " + item.getProductId());
            }
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                itemResponses.add(new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ));
            }
        }

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}

