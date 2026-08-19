package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CarritoResponse {
    private Long carritoId;
    private List<CarritoItemResponse> items;
    private Integer totalItems;
    private BigDecimal total;

    public CarritoResponse() {}

    public CarritoResponse(Long carritoId, List<CarritoItemResponse> items, Integer totalItems, BigDecimal total) {
        this.carritoId = carritoId;
        this.items = items;
        this.totalItems = totalItems;
        this.total = total;
    }

    public Long getCarritoId() { return carritoId; }
    public void setCarritoId(Long carritoId) { this.carritoId = carritoId; }

    public List<CarritoItemResponse> getItems() { return items; }
    public void setItems(List<CarritoItemResponse> items) { this.items = items; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}