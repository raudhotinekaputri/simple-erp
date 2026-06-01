package com.example.simple_erp.dto;

import java.math.BigDecimal;

public record MonthlySalesResponse(
        String month,
        BigDecimal totalSales
) {
}