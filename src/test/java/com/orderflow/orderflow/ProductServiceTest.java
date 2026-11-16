package com.orderflow.orderflow;

import com.orderflow.orderflow.model.Product;
import com.orderflow.orderflow.repository.ProductRepository;
import com.orderflow.orderflow.service.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldSaveProductSuccessfully() {
        // Arrange
        Product inputProduct = new Product(null, "Test Product", "Test Description", new BigDecimal("99.99"), 10, true);
        Product savedProduct = new Product(1L, "Test Product", "Test Description", new BigDecimal("99.99"), 10, true);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(inputProduct);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(new BigDecimal("99.99"), result.getPrice());
        assertEquals(10, result.getStockQuantity());
        assertTrue(result.getActive());

        verify(productRepository).save(inputProduct);
    }
}
  