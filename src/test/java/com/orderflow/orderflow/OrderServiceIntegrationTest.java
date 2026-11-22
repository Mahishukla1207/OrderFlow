package com.orderflow.orderflow;

import com.orderflow.orderflow.dto.OrderItemRequest;
import com.orderflow.orderflow.dto.OrderRequest;
import com.orderflow.orderflow.dto.OrderResponse;
import com.orderflow.orderflow.exception.InsufficientStockException;
import com.orderflow.orderflow.model.Order;
import com.orderflow.orderflow.model.OrderStatus;
import com.orderflow.orderflow.model.Product;
import com.orderflow.orderflow.repository.OrderRepository;
import com.orderflow.orderflow.repository.ProductRepository;
import com.orderflow.orderflow.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldPlaceOrderSuccessfullyAndPersistToDatabase() {
        // Arrange
        Product product = new Product(null, "Wireless Headphones", "Noise cancelling", new BigDecimal("150.00"), 20, true);
        Product savedProduct = productRepository.save(product);

        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(savedProduct.getId(), 3)));

        // Act
        OrderResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response.getId());
        assertEquals(new BigDecimal("450.00"), response.getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        // Verify database persistence of Order and cascaded OrderItem
        Optional<Order> fetchedOrderOpt = orderRepository.findByIdWithItems(response.getId());
        assertTrue(fetchedOrderOpt.isPresent());
        Order fetchedOrder = fetchedOrderOpt.get();
        assertEquals(1, fetchedOrder.getOrderItems().size());
        assertEquals(3, fetchedOrder.getOrderItems().get(0).getQuantity());
        assertEquals(new BigDecimal("150.00"), fetchedOrder.getOrderItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("450.00"), fetchedOrder.getOrderItems().get(0).getSubtotal());

        // Verify product stock in database was decremented
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(17, updatedProduct.getStockQuantity());
    }

    @Test
    void shouldRollbackTransactionWhenSubsequentItemFails() {
        // Arrange: Product A has 10 units, Product B has 2 units
        Product productA = productRepository.save(new Product(null, "Product A", "A", new BigDecimal("100.00"), 10, true));
        Product productB = productRepository.save(new Product(null, "Product B", "B", new BigDecimal("50.00"), 2, true));

        long initialOrderCount = orderRepository.count();

        // Request requests 4 of Product A (valid) and 5 of Product B (fails due to insufficient stock)
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(productA.getId(), 4),
                new OrderItemRequest(productB.getId(), 5)
        ));

        // Act & Assert: Exception is thrown on the second item
        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(request));

        // Verify rollback: Product A's stock was restored to 10 in the database
        Product reloadedProductA = productRepository.findById(productA.getId()).orElseThrow();
        assertEquals(10, reloadedProductA.getStockQuantity(), "Product A stock should remain 10 after rollback");

        // Product B's stock unchanged
        Product reloadedProductB = productRepository.findById(productB.getId()).orElseThrow();
        assertEquals(2, reloadedProductB.getStockQuantity(), "Product B stock should remain 2");

        // Verify no order was saved
        assertEquals(initialOrderCount, orderRepository.count(), "No order should be created when transaction rolls back");
    }

    @Test
    void shouldPreventOversellingUnderConcurrentOrders() throws Exception {
        // Arrange: Stock of 5. Two concurrent buyers try to order 3 each (Total: 6 > 5)
        Product product = productRepository.save(new Product(null, "Hot Ticket", "Limited item", new BigDecimal("50.00"), 5, true));
        Long productId = product.getId();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        List<OrderResponse> successfulOrders = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait for both threads to be ready
                    OrderRequest req = new OrderRequest(List.of(new OrderItemRequest(productId, 3)));
                    OrderResponse res = orderService.placeOrder(req);
                    successfulOrders.add(res);
                } catch (Throwable t) {
                    exceptions.add(t);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // trigger both threads simultaneously
        finishLatch.await();
        executor.shutdown();

        // Assert: Exactly 1 order succeeded, exactly 1 order failed due to InsufficientStockException
        assertEquals(1, successfulOrders.size(), "Only 1 concurrent order should succeed");
        assertEquals(1, exceptions.size(), "1 concurrent order should fail");
        assertTrue(exceptions.get(0) instanceof InsufficientStockException, "Failure should be InsufficientStockException");

        // Product stock in database must be exactly 2 (5 - 3 = 2), never negative!
        Product finalProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(2, finalProduct.getStockQuantity(), "Final product stock must be 2");
    }

    @Test
    void shouldRetrieveOrderWithItemsAndProductNamesWithoutLazyInitializationException() {
        // Arrange
        Product product = productRepository.save(new Product(null, "Mechanical Keyboard", "RGB Gaming", new BigDecimal("120.00"), 10, true));
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(product.getId(), 2)));
        OrderResponse placedOrder = orderService.placeOrder(request);

        // Act - Retrieve order via getOrderById (uses findByIdWithItems)
        Optional<OrderResponse> fetched = orderService.getOrderById(placedOrder.getId());

        // Assert
        assertTrue(fetched.isPresent());
        OrderResponse orderResponse = fetched.get();
        assertEquals(1, orderResponse.getItems().size());
        assertEquals("Mechanical Keyboard", orderResponse.getItems().get(0).getProductName());
        assertEquals(new BigDecimal("240.00"), orderResponse.getTotalAmount());
    }
}
