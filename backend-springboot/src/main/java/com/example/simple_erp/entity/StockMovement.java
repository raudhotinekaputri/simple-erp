package com.example.simple_erp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;

    private String movementType;
    private Integer quantity;

    private String referenceType;
    private Long referenceId;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDateTime createdAt;

    public StockMovement() {
    }

    public StockMovement(
            Long productId,
            String productName,
            String movementType,
            Integer quantity,
            String referenceType,
            Long referenceId,
            String note
    ) {
        this.productId = productId;
        this.productName = productName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getMovementType() {
        return movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}