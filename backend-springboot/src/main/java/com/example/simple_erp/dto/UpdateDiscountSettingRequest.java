package com.example.simple_erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDiscountSettingRequest(
        @NotNull(message = "One box discount is required")
        @DecimalMin(value = "0.0", message = "Discount cannot be negative")
        BigDecimal oneBoxDiscountPercent,

        @NotNull(message = "Three box discount is required")
        @DecimalMin(value = "0.0", message = "Discount cannot be negative")
        BigDecimal threeBoxDiscountPercent
) {
}