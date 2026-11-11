package com.orderflow.orderflow.service;

import com.orderflow.orderflow.model.Product;
import com.orderflow.orderflow.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        validateProduct(product);

        product.setId(null);
        if (product.getActive() == null) {
            product.setActive(Boolean.TRUE);
        }

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        validateProduct(updatedProduct);

        Optional<Product> existingOptional = productRepository.findById(id);
        if (existingOptional.isEmpty()) {
            return Optional.empty();
        }

        Product existingProduct = existingOptional.get();
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        if (updatedProduct.getActive() != null) {
            existingProduct.setActive(updatedProduct.getActive());
        }

        return Optional.of(productRepository.save(existingProduct));
    }

    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name must not be null or blank");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must not be null or negative");
        }
    }
}

