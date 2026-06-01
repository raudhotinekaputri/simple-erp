package com.example.simple_erp.dto;

import com.example.simple_erp.entity.Product;
import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        Long totalProducts,
        BigDecimal totalRevenue,
        BigDecimal grossProfit,
        List<Product> lowStockProducts
) {
}