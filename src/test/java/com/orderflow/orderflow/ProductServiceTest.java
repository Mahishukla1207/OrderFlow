package com.orderflow.orderflow;

import com.orderflow.orderflow.model.Product;
import com.orderflow.orderflow.repository.ProductRepository;
import com.orderflow.orderflow.service.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void createProduct_shouldSetActiveTrueWhenActiveIsNull() {
        // Arrange
        Product inputProduct = new Product(null, "Test Product", "Test Description", new BigDecimal("99.99"), 10, null);
        Product savedProduct = new Product(1L, "Test Product", "Test Description", new BigDecimal("99.99"), 10, true);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(inputProduct);

        // Assert
        assertNotNull(result);
        assertTrue(result.getActive());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertTrue(productCaptor.getValue().getActive());
    }

    @Test
    void createProduct_shouldThrowExceptionWhenPriceIsNegative() {
        // Arrange
        Product inputProduct = new Product(null, "Test Product", "Test Description", new BigDecimal("-10.00"), 10, true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(inputProduct);
        });

        assertEquals("Product price must be greater than zero", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenNameIsBlank() {
        // Arrange
        Product inputProduct = new Product(null, "   ", "Test Description", new BigDecimal("99.99"), 10, true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(inputProduct);
        });

        assertEquals("Product name must not be null or blank", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenStockQuantityIsNegative() {
       // Arrange
       Product inputProduct = new Product(null, "Test Product", "Test Description", new BigDecimal("99.99"), -5, true);

    // Act & Assert
    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(inputProduct)
    );

    assertEquals(
            "Stock quantity must not be null or negative",
            exception.getMessage()
    );

    verify(productRepository, never()).save(any(Product.class));
}

@Test
void getAllProducts_shouldReturnAllProducts() {
    // Arrange
    List<Product> products = List.of(
            new Product(1L, "Mouse", "Wireless Mouse", new BigDecimal("999.00"), 10, true),
            new Product(2L, "Keyboard", "Mechanical Keyboard", new BigDecimal("1999.00"), 5, true)
    );

    when(productRepository.findAll()).thenReturn(products);

    // Act
    List<Product> result = productService.getAllProducts();

    // Assert
    assertEquals(2, result.size());
    assertEquals("Mouse", result.get(0).getName());
    assertEquals("Keyboard", result.get(1).getName());

    verify(productRepository).findAll();
}

@Test
void getProductById_shouldReturnProductWhenFound() {
    // Arrange
    Product product = new Product(
            1L, "Mouse", "Wireless Mouse",
            new BigDecimal("999.00"), 10, true
    );

    when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

    // Act
    Optional<Product> result = productService.getProductById(1L);

    // Assert
    assertTrue(result.isPresent());
    assertEquals("Mouse", result.get().getName());

    verify(productRepository).findById(1L);
}

@Test
void getProductById_shouldReturnEmptyWhenProductNotFound() {
    // Arrange
    when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

    // Act
    Optional<Product> result = productService.getProductById(99L);

    // Assert
    assertTrue(result.isEmpty());

    verify(productRepository).findById(99L);
}

@Test
void updateProduct_shouldUpdateProductWhenFound() {
    // Arrange
    Product existingProduct = new Product(
            1L, "Old Mouse", "Old Description",
            new BigDecimal("500.00"), 5, true
    );

    Product updatedProduct = new Product(
            null, "Gaming Mouse", "RGB Wireless Mouse",
            new BigDecimal("1499.00"), 20, false
    );

    when(productRepository.findById(1L))
            .thenReturn(Optional.of(existingProduct));

    when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Optional<Product> result =
            productService.updateProduct(1L, updatedProduct);

    // Assert
    assertTrue(result.isPresent());
    assertEquals("Gaming Mouse", result.get().getName());
    assertEquals("RGB Wireless Mouse", result.get().getDescription());
    assertEquals(new BigDecimal("1499.00"), result.get().getPrice());
    assertEquals(20, result.get().getStockQuantity());
    assertEquals(false, result.get().getActive());
    assertEquals(1L, result.get().getId());

    verify(productRepository).findById(1L);
    verify(productRepository).save(existingProduct);
}

@Test
void updateProduct_shouldReturnEmptyWhenProductNotFound() {
    // Arrange
    Product updatedProduct = new Product(
            null,
            "Gaming Mouse",
            "RGB Wireless Mouse",
            new BigDecimal("1499.00"),
            20,
            true
    );

    when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

    // Act
    Optional<Product> result =
            productService.updateProduct(99L, updatedProduct);

    // Assert
    assertTrue(result.isEmpty());

    verify(productRepository).findById(99L);
    verify(productRepository, never()).save(any(Product.class));
}

@Test
void deleteProduct_shouldReturnTrueWhenProductDeleted() {
    // Arrange
    when(productRepository.deleteById(1L)).thenReturn(true);

    // Act
    boolean result = productService.deleteProduct(1L);

    // Assert
    assertTrue(result);
    verify(productRepository).deleteById(1L);
}

@Test
void deleteProduct_shouldReturnFalseWhenProductNotFound() {
    // Arrange
    when(productRepository.deleteById(99L)).thenReturn(false);

    // Act
    boolean result = productService.deleteProduct(99L);

    // Assert
    assertFalse(result);
    verify(productRepository).deleteById(99L);
}
}
