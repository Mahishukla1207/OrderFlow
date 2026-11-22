package com.orderflow.orderflow.controller;

import com.orderflow.orderflow.dto.ProductResponse;
import com.orderflow.orderflow.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.orderflow.orderflow.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
void createProduct_shouldReturnCreated() throws Exception {

    // Arrange
    Product createdProduct = new Product(1L, "Gaming Mouse", "Wireless gaming mouse", new BigDecimal("1499.00"), 20, true);

    when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

    // Act & Assert
    mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON).content("""
                            {
                                "name": "Gaming Mouse",
                                "description": "Wireless gaming mouse",
                                "price": 1499.00,
                                "stockQuantity": 20
                            }
                            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Gaming Mouse"))
            .andExpect(jsonPath("$.active").value(true));

    verify(productService).createProduct(any(Product.class));
}

@Test
void createProduct_shouldReturnBadRequestWhenPriceIsInvalid() throws Exception {

    // Act & Assert
    mockMvc.perform(post("/api/v1/products")
                    .contentType(APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Gaming Mouse",
                                "description": "Wireless gaming mouse",
                                "price": -100,
                                "stockQuantity": 20
                            }
                            """))
            .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(Product.class));
}

@Test
void createProduct_shouldReturnValidationErrorResponse() throws Exception {

    mockMvc.perform(post("/api/v1/products")
                    .contentType(APPLICATION_JSON)
                    .content("""
                            {
                                "name": "",
                                "price": -100,
                                "stockQuantity": -5
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isNotEmpty());

    verify(productService, never()).createProduct(any(Product.class));
}

@Test
void getAllProducts_shouldReturnProductList() throws Exception {

    // Arrange
    Product product1 = new Product(
            1L, "Gaming Mouse", "Wireless mouse",
            new BigDecimal("1499.00"), 20, true
    );

    Product product2 = new Product(
            2L, "Keyboard", "Mechanical keyboard",
            new BigDecimal("2499.00"), 15, true
    );

    when(productService.getAllProducts())
            .thenReturn(List.of(product1, product2));

    // Act & Assert
    mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Gaming Mouse"))
            .andExpect(jsonPath("$[1].name").value("Keyboard"));

    verify(productService).getAllProducts();
}

@Test
void getProductById_shouldReturnProductWhenFound() throws Exception {

    // Arrange
    Product product = new Product(
            1L,
            "Gaming Mouse",
            "Wireless mouse",
            new BigDecimal("1499.00"),
            20,
            true
    );

    when(productService.getProductById(1L))
            .thenReturn(Optional.of(product));

    // Act & Assert
    mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Gaming Mouse"))
            .andExpect(jsonPath("$.price").value(1499.00));

    verify(productService).getProductById(1L);
}

@Test
void getProductById_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

    // Arrange
    when(productService.getProductById(99L))
            .thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(get("/api/v1/products/99"))
            .andExpect(status().isNotFound());

    verify(productService).getProductById(99L);
}

@Test
void updateProduct_shouldReturnUpdatedProduct() throws Exception {

    // Arrange
    Product updatedProduct = new Product(
            1L,
            "Gaming Keyboard",
            "RGB mechanical keyboard",
            new BigDecimal("2499.00"),
            15,
            true
    );

    when(productService.updateProduct(
            org.mockito.ArgumentMatchers.eq(1L),
            any(Product.class)
    )).thenReturn(Optional.of(updatedProduct));

    // Act & Assert
    mockMvc.perform(put("/api/v1/products/1")
                    .contentType(APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Gaming Keyboard",
                                "description": "RGB mechanical keyboard",
                                "price": 2499.00,
                                "stockQuantity": 15
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Gaming Keyboard"))
            .andExpect(jsonPath("$.price").value(2499.00));

    verify(productService).updateProduct(
            org.mockito.ArgumentMatchers.eq(1L),
            any(Product.class)
    );
}

@Test
void updateProduct_shouldUpdateActiveStatusToFalseWhenProvided() throws Exception {
    // Arrange
    Product updatedProduct = new Product(
            52L,
            "USB-C Hub",
            "Multiport USB-C adapter",
            new BigDecimal("1500.00"),
            1,
            false
    );

    org.mockito.ArgumentCaptor<Product> productCaptor = org.mockito.ArgumentCaptor.forClass(Product.class);
    when(productService.updateProduct(eq(52L), productCaptor.capture()))
            .thenReturn(Optional.of(updatedProduct));

    // Act & Assert
    mockMvc.perform(put("/api/v1/products/52")
                    .contentType(APPLICATION_JSON)
                    .content("""
                            {
                                "name": "USB-C Hub",
                                "description": "Multiport USB-C adapter",
                                "price": 1500.00,
                                "stockQuantity": 1,
                                "active": false
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(52))
            .andExpect(jsonPath("$.name").value("USB-C Hub"))
            .andExpect(jsonPath("$.price").value(1500.00))
            .andExpect(jsonPath("$.stockQuantity").value(1))
            .andExpect(jsonPath("$.active").value(false));

    org.junit.jupiter.api.Assertions.assertEquals(Boolean.FALSE, productCaptor.getValue().getActive());
    verify(productService).updateProduct(eq(52L), any(Product.class));
}

@Test
void updateProduct_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

    // Arrange
    when(productService.updateProduct(eq(99L), any(Product.class)))
            .thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(put("/api/v1/products/99")
                    .contentType(APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Gaming Keyboard",
                                "description": "RGB mechanical keyboard",
                                "price": 2499.00,
                                "stockQuantity": 15
                            }
                            """))
            .andExpect(status().isNotFound());

    verify(productService).updateProduct(eq(99L), any(Product.class));
}

@Test
void deleteProduct_shouldReturnNoContentWhenProductDeleted() throws Exception {

    // Arrange
    when(productService.deleteProduct(1L)).thenReturn(true);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/products/1"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

    verify(productService).deleteProduct(1L);
}

@Test
void deleteProduct_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

    // Arrange
    when(productService.deleteProduct(99L)).thenReturn(false);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/products/99"))
            .andExpect(status().isNotFound());

    verify(productService).deleteProduct(99L);
}
}