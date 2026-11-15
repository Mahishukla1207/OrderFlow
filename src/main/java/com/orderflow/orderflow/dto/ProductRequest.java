package com.orderflow.orderflow.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProductRequest {
    @NotBlank //String cannot be null, empty, or whitespace
    private String name;
    private String description;
    @NotNull  //Value cannot be null
    @Positive //Number must be positive as price cannot be null or negative
    private BigDecimal price;
    @NotNull
    @Min(0)  //Number must be atleast 0
    private Integer stockQuantity;

    // No-argument constructor
    public ProductRequest() {
    }

    // Parameterized constructor
    public ProductRequest(String name, String description, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
