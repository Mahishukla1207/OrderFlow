package com.orderflow.orderflow.repository;

import com.orderflow.orderflow.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private final Map<Long, Product> productStore = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (product.getId() == null) {
            product.setId(idGenerator.incrementAndGet());
        }
        productStore.put(product.getId(), product);
        return product;
    }

    public List<Product> findAll() {
        return new ArrayList<>(productStore.values());
    }

    public Optional<Product> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(productStore.get(id));
    }

    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return productStore.remove(id) != null;
    }
}

