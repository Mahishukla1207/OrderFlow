package com.orderflow.orderflow;

import com.orderflow.orderflow.dto.OrderItemRequest;
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
import com.orderflow.orderflow.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_shouldSucceedWithSingleItem() {
        // Arrange
        Product product = new Product(1L, "Laptop", "High end", new BigDecimal("1200.00"), 10, true);
        when(productRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(1L, 2)));

        // Act
        OrderResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("2400.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals("Laptop", response.getItems().get(0).getProductName());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("1200.00"), response.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("2400.00"), response.getItems().get(0).getSubtotal());

        // Verify product stock decreased
        assertEquals(8, product.getStockQuantity());
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_shouldSucceedWithMultipleItemsAndCalculateTotalsCorrectly() {
        // Arrange
        Product p1 = new Product(1L, "Keyboard", "Mechanical", new BigDecimal("100.00"), 15, true);
        Product p2 = new Product(2L, "Mouse", "Wireless", new BigDecimal("50.00"), 20, true);

        when(productRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findByIdWithPessimisticLock(2L)).thenReturn(Optional.of(p2));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(200L);
            return o;
        });

        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(1L, 2), // 2 * 100 = 200
                new OrderItemRequest(2L, 3)  // 3 * 50 = 150
        ));

        // Act
        OrderResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("350.00"), response.getTotalAmount());
        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("200.00"), response.getItems().get(0).getSubtotal());
        assertEquals(new BigDecimal("150.00"), response.getItems().get(1).getSubtotal());

        // Verify stock decreases
        assertEquals(13, p1.getStockQuantity());
        assertEquals(17, p2.getStockQuantity());
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        when(productRepository.findByIdWithPessimisticLock(99L)).thenReturn(Optional.empty());
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(99L, 1)));

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> orderService.placeOrder(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenProductIsInactive() {
        // Arrange
        Product inactiveProduct = new Product(1L, "Discontinued Item", "Desc", new BigDecimal("50.00"), 10, false);
        when(productRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(inactiveProduct));
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(1L, 1)));

        // Act & Assert
        assertThrows(InactiveProductException.class, () -> orderService.placeOrder(request));
        verify(productRepository, never()).save(inactiveProduct);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenStockIsInsufficient() {
        // Arrange
        Product product = new Product(1L, "Limited Item", "Desc", new BigDecimal("10.00"), 3, true);
        when(productRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(product));
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(1L, 5)));

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(request));
        assertEquals(3, product.getStockQuantity()); // Stock unchanged
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenQuantityIsInvalid() {
        // Arrange
        OrderRequest zeroQtyRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 0)));
        OrderRequest negativeQtyRequest = new OrderRequest(List.of(new OrderItemRequest(1L, -2)));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(zeroQtyRequest));
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(negativeQtyRequest));
        verify(productRepository, never()).findByIdWithPessimisticLock(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenProductIdIsZeroOrNegative() {
        // Arrange
        OrderRequest zeroIdRequest = new OrderRequest(List.of(new OrderItemRequest(0L, 1)));
        OrderRequest negativeIdRequest = new OrderRequest(List.of(new OrderItemRequest(-5L, 1)));

        // Act & Assert
        IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(zeroIdRequest));
        assertTrue(exZero.getMessage().contains("Product ID must be a positive number"));

        IllegalArgumentException exNeg = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(negativeIdRequest));
        assertTrue(exNeg.getMessage().contains("Product ID must be a positive number"));

        verify(productRepository, never()).findByIdWithPessimisticLock(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_shouldAcquireLocksInAscendingProductIdOrderEvenWhenRequestOrderIsDescending() {
        // Arrange - Request provides product 20 first, then product 10
        Product p10 = new Product(10L, "Item 10", "Desc 10", new BigDecimal("30.00"), 5, true);
        Product p20 = new Product(20L, "Item 20", "Desc 20", new BigDecimal("40.00"), 5, true);

        when(productRepository.findByIdWithPessimisticLock(10L)).thenReturn(Optional.of(p10));
        when(productRepository.findByIdWithPessimisticLock(20L)).thenReturn(Optional.of(p20));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(20L, 1),
                new OrderItemRequest(10L, 2)
        ));

        // Act
        OrderResponse response = orderService.placeOrder(request);

        // Assert - Lock acquisition order must be ascending by product ID (10L first, then 20L)
        InOrder inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).findByIdWithPessimisticLock(10L);
        inOrder.verify(productRepository).findByIdWithPessimisticLock(20L);

        assertEquals(new BigDecimal("100.00"), response.getTotalAmount()); // 2*30 + 1*40 = 100
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenDuplicateProductIdsProvided() {
        // Arrange - Request has duplicate product ID 1L
        OrderRequest duplicateRequest = new OrderRequest(List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(1L, 3)
        ));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(duplicateRequest));
        assertTrue(ex.getMessage().contains("Duplicate product ID"));
        verify(productRepository, never()).findByIdWithPessimisticLock(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_shouldThrowExceptionWhenRequestOrItemsEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(null));
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(new OrderRequest(null)));
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(new OrderRequest(List.of())));
    }

    @Test
    void placeOrder_shouldSaveOrderWithAssociationUsingAddOrderItemHelper() {
        // Arrange
        Product product = new Product(1L, "Book", "Fiction", new BigDecimal("25.00"), 10, true);
        when(productRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(50L);
            return o;
        });

        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(1L, 2)));

        // Act
        orderService.placeOrder(request);

        // Assert Order entity capture
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(new BigDecimal("50.00"), capturedOrder.getTotalAmount());
        assertEquals(OrderStatus.PENDING, capturedOrder.getStatus());
        assertNotNull(capturedOrder.getCreatedAt());
        assertEquals(1, capturedOrder.getOrderItems().size());
        assertEquals(capturedOrder, capturedOrder.getOrderItems().get(0).getOrder());
    }

    @Test
    void getOrderById_shouldReturnOrderWhenFound() {
        // Arrange
        Product product = new Product(1L, "Book", "Fiction", new BigDecimal("25.00"), 10, true);
        Order order = new Order(new BigDecimal("100.00"), OrderStatus.CONFIRMED, LocalDateTime.now());
        order.setId(10L);
        order.addOrderItem(new OrderItem(product, 4, new BigDecimal("25.00"), new BigDecimal("100.00")));
        when(orderRepository.findByIdWithItems(10L)).thenReturn(Optional.of(order));

        // Act
        Optional<OrderResponse> result = orderService.getOrderById(10L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
        assertEquals(OrderStatus.CONFIRMED, result.get().getStatus());
        assertEquals(1, result.get().getItems().size());
        assertEquals("Book", result.get().getItems().get(0).getProductName());
    }

    @Test
    void getOrderById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(orderRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

        // Act
        Optional<OrderResponse> result = orderService.getOrderById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }
}
