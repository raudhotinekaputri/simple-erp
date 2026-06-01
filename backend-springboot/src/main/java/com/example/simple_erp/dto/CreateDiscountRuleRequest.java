package com.example.simple_erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDiscountRuleRequest(
        @NotBlank(message = "Discount name is required")
        String name,

        @NotNull(message = "Minimum quantity is required")
        @Min(value = 1, message = "Minimum quantity must be at least 1")
        Integer minimumQuantity,

        @NotNull(message = "Discount percent is required")
        @DecimalMin(value = "0.0", message = "Discount cannot be negative")
        BigDecimal discountPercent,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {
}