package com.example.simple_erp.service;

import com.example.simple_erp.dto.BusinessInsightResponse;
import com.example.simple_erp.entity.Product;
import com.example.simple_erp.repository.ProductRepository;
import com.example.simple_erp.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BusinessInsightService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;

    public BusinessInsightService(
            SalesOrderRepository salesOrderRepository,
            ProductRepository productRepository
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
    }

    public BusinessInsightResponse getBusinessInsight(LocalDate startDate, LocalDate endDate) {
        boolean hasDateFilter = startDate != null && endDate != null;

        Long totalOrders = hasDateFilter
                ? salesOrderRepository.countOrdersByDateRange(startDate, endDate)
                : salesOrderRepository.count();

        Long pendingPaymentOrders = hasDateFilter
                ? salesOrderRepository.countPendingPaymentOrdersByDateRange(startDate, endDate)
                : salesOrderRepository.countByPaymentStatus("Unpaid");

        LocalDate paymentRiskThreshold = LocalDate.now().minusDays(3);
        LocalDate deliveryRiskThreshold = LocalDate.now().minusDays(7);

        Long paymentRiskOrders = hasDateFilter
                ? salesOrderRepository.countPaymentRiskOrdersByDateRange(
                        paymentRiskThreshold,
                        startDate,
                        endDate
                )
                : salesOrderRepository.countPaymentRiskOrders(paymentRiskThreshold);

        Long delayedShipmentOrders = hasDateFilter
                ? salesOrderRepository.countDelayedShipmentOrdersByDateRange(
                        deliveryRiskThreshold,
                        startDate,
                        endDate
                )
                : salesOrderRepository.countDelayedShipmentOrders(deliveryRiskThreshold);

        Long cancelledOrders = hasDateFilter
                ? salesOrderRepository.countByOrderStatusAndDateRange("CANCELLED", startDate, endDate)
                : salesOrderRepository.countByOrderStatus("CANCELLED");

        Long refundRequestedOrders = hasDateFilter
                ? salesOrderRepository.countByRefundStatusAndDateRange("Requested", startDate, endDate)
                : salesOrderRepository.countByRefundStatus("Requested");

        BigDecimal totalRevenue = hasDateFilter
                ? salesOrderRepository.getTotalRevenueByDateRange(startDate, endDate)
                : salesOrderRepository.getTotalRevenue();

        BigDecimal averageOrderValue = calculatePercentageBase(totalRevenue, totalOrders);

        BigDecimal refundRate = calculateRate(refundRequestedOrders, totalOrders);
        BigDecimal cancellationRate = calculateRate(cancelledOrders, totalOrders);

        Long ordersWithDiscount = hasDateFilter
                ? salesOrderRepository.countOrdersWithDiscountByDateRange(startDate, endDate)
                : salesOrderRepository.countOrdersWithDiscount();

        BigDecimal discountUsageRate = calculateRate(ordersWithDiscount, totalOrders);

        List<Object[]> bestSellingRows = hasDateFilter
                ? salesOrderRepository.getBestSellingProductsByDateRange(startDate, endDate)
                : salesOrderRepository.getBestSellingProducts();

        String bestSellingProduct = bestSellingRows.isEmpty()
                ? "-"
                : String.valueOf(bestSellingRows.get(0)[0]);

        List<Object[]> revenueRows = hasDateFilter
                ? salesOrderRepository.getRevenueByProductByDateRange(startDate, endDate)
                : salesOrderRepository.getRevenueByProduct();

        Map<String, BigDecimal> revenueByProduct = new LinkedHashMap<>();

        for (Object[] row : revenueRows) {
            revenueByProduct.put(
                    String.valueOf(row[0]),
                    (BigDecimal) row[1]
            );
        }

        Long lowStockProducts = productRepository.findAll()
                .stream()
                .filter(product -> product.getStock() <= product.getMinStock())
                .count();

        List<String> insights = buildInsights(
                totalOrders,
                pendingPaymentOrders,
                paymentRiskOrders,
                delayedShipmentOrders,
                cancelledOrders,
                refundRequestedOrders,
                bestSellingProduct,
                lowStockProducts,
                averageOrderValue,
                refundRate,
                cancellationRate,
                discountUsageRate,
                revenueByProduct
        );

        return new BusinessInsightResponse(
                totalOrders,
                pendingPaymentOrders,
                paymentRiskOrders,
                delayedShipmentOrders,
                cancelledOrders,
                refundRequestedOrders,
                bestSellingProduct,
                lowStockProducts,
                averageOrderValue,
                refundRate,
                cancellationRate,
                discountUsageRate,
                revenueByProduct,
                insights
        );
    }

    private List<String> buildInsights(
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
            Map<String, BigDecimal> revenueByProduct
    ) {
        List<String> insights = new ArrayList<>();

        if (totalOrders == 0) {
            insights.add("Belum ada sales order pada periode ini, sehingga belum ada insight transaksi yang bisa dianalisis.");
            return insights;
        }

        insights.add("Total terdapat " + totalOrders + " order dengan average order value sebesar Rp "
                + averageOrderValue.toPlainString() + ".");

        if (pendingPaymentOrders > 0) {
            insights.add("Terdapat " + pendingPaymentOrders + " order dengan status pembayaran unpaid yang perlu dipantau.");
        }

        if (paymentRiskOrders > 0) {
            insights.add("Terdapat " + paymentRiskOrders + " order unpaid lebih dari 3 hari berdasarkan order date. Order ini perlu follow-up pembayaran.");
        }

        if (delayedShipmentOrders > 0) {
            insights.add("Terdapat " + delayedShipmentOrders + " order aktif yang belum selesai dikirim lebih dari 7 hari berdasarkan order date. Perlu follow-up ekspedisi atau proses fulfillment.");
        }

        if (cancelledOrders > 0) {
            insights.add("Terdapat " + cancelledOrders + " order cancelled. Perlu dicek alasan pembatalan agar tidak berulang.");
        }

        if (refundRequestedOrders > 0) {
            insights.add("Terdapat " + refundRequestedOrders + " order refund requested. Perlu validasi bukti dan penyebab refund.");
        }

        if (!"-".equals(bestSellingProduct)) {
            insights.add("Produk dengan penjualan tertinggi adalah " + bestSellingProduct + ". Produk ini layak diprioritaskan untuk monitoring stok dan campaign.");
        }

        if (lowStockProducts > 0) {
            insights.add("Terdapat " + lowStockProducts + " produk low stock. Segera lakukan restock agar tidak menghambat penjualan.");
        }

        if (discountUsageRate.compareTo(BigDecimal.ZERO) > 0) {
            insights.add("Discount usage rate saat ini sebesar " + discountUsageRate.toPlainString() + "%. Evaluasi apakah diskon membantu meningkatkan order quantity atau revenue.");
        }

        insights.add("Refund rate berada di angka " + refundRate.toPlainString() + "% dan cancellation rate berada di angka " + cancellationRate.toPlainString() + "%.");

        return insights;
    }

    private BigDecimal calculateRate(Long value, Long total) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentageBase(BigDecimal value, Long total) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO;
        }

        return value.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}