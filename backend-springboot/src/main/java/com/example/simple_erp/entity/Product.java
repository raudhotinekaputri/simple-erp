package com.example.simple_erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer stock;
    private Integer minStock;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;

    private Boolean active = true;

    public Product() {
    }

    public Product(
            String name,
            Integer stock,
            Integer minStock,
            BigDecimal costPrice,
            BigDecimal sellingPrice
    ) {
        this.name = name;
        this.stock = stock;
        this.minStock = minStock;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}