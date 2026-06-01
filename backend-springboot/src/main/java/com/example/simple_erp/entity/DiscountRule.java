package com.example.simple_erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "discount_rules")
public class DiscountRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer minimumQuantity;
    private BigDecimal discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;

    public DiscountRule() {
    }

    public DiscountRule(
            String name,
            Integer minimumQuantity,
            BigDecimal discountPercent,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.name = name;
        this.minimumQuantity = minimumQuantity;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStatus() {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "UPCOMING";
        }

        if (today.isAfter(endDate)) {
            return "EXPIRED";
        }

        return "ACTIVE";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}