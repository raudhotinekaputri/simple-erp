package com.example.simple_erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "discount_settings")
public class DiscountSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal oneBoxDiscountPercent;
    private BigDecimal threeBoxDiscountPercent;

    public DiscountSetting() {
    }

    public DiscountSetting(BigDecimal oneBoxDiscountPercent, BigDecimal threeBoxDiscountPercent) {
        this.oneBoxDiscountPercent = oneBoxDiscountPercent;
        this.threeBoxDiscountPercent = threeBoxDiscountPercent;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getOneBoxDiscountPercent() {
        return oneBoxDiscountPercent;
    }

    public BigDecimal getThreeBoxDiscountPercent() {
        return threeBoxDiscountPercent;
    }

    public void setOneBoxDiscountPercent(BigDecimal oneBoxDiscountPercent) {
        this.oneBoxDiscountPercent = oneBoxDiscountPercent;
    }

    public void setThreeBoxDiscountPercent(BigDecimal threeBoxDiscountPercent) {
        this.threeBoxDiscountPercent = threeBoxDiscountPercent;
    }
}