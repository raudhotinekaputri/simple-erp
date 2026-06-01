package com.example.simple_erp.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BusinessInsightResponse(
        Long totalOrders,
        Long pendingPaymentOrders,
        Long paymentRiskOrders,
        Long delayedShipmentOrders,
        Long cancelledOrders,
        Long refundRequestedOrders,
        String bestSellingProduct,
        Long lowStockProducts,
        BigDecimal averageOrderValue,
        BigDecimal refundRate,
        BigDecimal cancellationRate,
        BigDecimal discountUsageRate,
        Map<String, BigDecimal> revenueByProduct,
        List<String> insights
) {
}