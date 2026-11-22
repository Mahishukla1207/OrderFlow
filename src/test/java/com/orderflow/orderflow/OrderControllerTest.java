package com.orderflow.orderflow.controller;

import com.orderflow.orderflow.dto.OrderItemResponse;
import com.orderflow.orderflow.dto.OrderRequest;
import com.orderflow.orderflow.dto.OrderResponse;
import com.orderflow.orderflow.exception.InactiveProductException;
import com.orderflow.orderflow.exception.InsufficientStockException;
import com.orderflow.orderflow.exception.ProductNotFoundException;
import com.orderflow.orderflow.model.OrderStatus;
import com.orderflow.orderflow.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void placeOrder_shouldReturnCreatedWhenRequestIsValid() throws Exception {
        // Arrange
        OrderItemResponse itemResponse = new OrderItemResponse(
                1L, 10L, "Gaming Keyboard", 2, new BigDecimal("1200.00"), new BigDecimal("2400.00")
        );
        OrderResponse orderResponse = new OrderResponse(
                100L, new BigDecimal("2400.00"), OrderStatus.PENDING, LocalDateTime.now(), List.of(itemResponse)
        );

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": 10,
                                            "quantity": 2
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.totalAmount").value(2400.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.items[0].productName").value("Gaming Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1200.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(2400.00));

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenItemsListIsEmpty() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty());

        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenProductIdIsInvalid() throws Exception {
        // Act & Assert (negative productId and zero quantity)
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": -5,
                                            "quantity": 0
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages").isArray());

        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenItemHasNullProductId() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": null,
                                            "quantity": 2
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnNotFoundWhenProductNotFoundExceptionThrown() throws Exception {
        // Arrange
        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new ProductNotFoundException(99L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": 99,
                                            "quantity": 1
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Product Not Found"))
                .andExpect(jsonPath("$.messages[0]").value("Product not found with ID: 99"));

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenInsufficientStockExceptionThrown() throws Exception {
        // Arrange
        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product: Gaming Keyboard"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": 10,
                                            "quantity": 50
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Insufficient Stock"))
                .andExpect(jsonPath("$.messages[0]").value("Insufficient stock for product: Gaming Keyboard"));

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenInactiveProductExceptionThrown() throws Exception {
        // Arrange
        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new InactiveProductException(10L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": 10,
                                            "quantity": 1
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Inactive Product"))
                .andExpect(jsonPath("$.messages[0]").value("Product is inactive with ID: 10"));

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenDuplicateProductIdExceptionThrown() throws Exception {
        // Arrange - Service detects duplicate product IDs
        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Duplicate product ID found in order request: 10"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        {
                                            "productId": 10,
                                            "quantity": 1
                                        },
                                        {
                                            "productId": 10,
                                            "quantity": 2
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.messages[0]").value("Duplicate product ID found in order request: 10"));

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

    @Test
    void getOrderById_shouldReturnOkWhenOrderExists() throws Exception {
        // Arrange
        OrderItemResponse itemResponse = new OrderItemResponse(
                1L, 10L, "Gaming Keyboard", 1, new BigDecimal("1200.00"), new BigDecimal("1200.00")
        );
        OrderResponse orderResponse = new OrderResponse(
                50L, new BigDecimal("1200.00"), OrderStatus.CONFIRMED, LocalDateTime.now(), List.of(itemResponse)
        );

        when(orderService.getOrderById(50L)).thenReturn(Optional.of(orderResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.totalAmount").value(1200.00))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("Gaming Keyboard"));

        verify(orderService).getOrderById(50L);
    }

    @Test
    void getOrderById_shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        // Arrange
        when(orderService.getOrderById(eq(999L))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(999L);
    }
}
