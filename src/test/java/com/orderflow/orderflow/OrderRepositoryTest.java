package com.orderflow.orderflow;

import com.orderflow.orderflow.model.Order;
import com.orderflow.orderflow.model.OrderItem;
import com.orderflow.orderflow.model.OrderStatus;
import com.orderflow.orderflow.model.Product;
import com.orderflow.orderflow.repository.OrderRepository;
import com.orderflow.orderflow.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndRetrieveOrderWithItems() {
        Product product = new Product(null, "Test Product", "Description", new BigDecimal("100.00"), 10, true);
        product = productRepository.save(product);

        Order order = new Order(new BigDecimal("200.00"), OrderStatus.PENDING, LocalDateTime.now());
        OrderItem item = new OrderItem(product, 2, new BigDecimal("100.00"), new BigDecimal("200.00"));
        order.addOrderItem(item);

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder.getId());
        Optional<Order> fetched = orderRepository.findById(savedOrder.getId());
        assertTrue(fetched.isPresent());
        assertEquals(1, fetched.get().getOrderItems().size());
        assertEquals(OrderStatus.PENDING, fetched.get().getStatus());
        assertEquals(new BigDecimal("200.00"), fetched.get().getTotalAmount());
    }

    @Test
    void shouldFindOrdersByStatus() {
        Order order = new Order(new BigDecimal("50.00"), OrderStatus.CONFIRMED, LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        List<Order> confirmedOrders = orderRepository.findByStatus(OrderStatus.CONFIRMED);
        assertFalse(confirmedOrders.isEmpty());
        assertTrue(confirmedOrders.stream().anyMatch(o -> o.getId().equals(savedOrder.getId())));
    }
}
