
package com.orderflow.orderflow.repository;

import com.orderflow.orderflow.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
