package com.orderflow.orderflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    private List<@Valid OrderItemRequest> items = new ArrayList<>();

    // No-argument constructor
    public OrderRequest() {
    }

    // Parameterized constructor
    public OrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    // Getters and Setters
    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
