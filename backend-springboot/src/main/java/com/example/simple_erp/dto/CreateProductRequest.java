package com.example.simple_erp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock,

        @NotNull(message = "Minimum stock is required")
        @Min(value = 0, message = "Minimum stock cannot be negative")
        Integer minStock,

        @NotNull(message = "Cost price is required")
        BigDecimal costPrice,

        @NotNull(message = "Selling price is required")
        BigDecimal sellingPrice
) {
}