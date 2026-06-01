package com.example.simple_erp.service;

import com.example.simple_erp.dto.DashboardResponse;
import com.example.simple_erp.dto.MonthlySalesResponse;
import com.example.simple_erp.entity.Product;
import com.example.simple_erp.repository.ProductRepository;
import com.example.simple_erp.repository.SalesOrderItemRepository;
import com.example.simple_erp.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    public DashboardService(
            ProductRepository productRepository,
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository
    ) {
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
    }

    public DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate) {
        boolean filtered = hasDateFilter(startDate, endDate);

        long totalProducts = productRepository.count();

        BigDecimal totalRevenue = filtered
                ? salesOrderRepository.getTotalRevenueByDateRange(startDate, endDate)
                : salesOrderRepository.getTotalRevenue();

        BigDecimal grossProfit = filtered
                ? salesOrderItemRepository.getGrossProfitByDateRange(startDate, endDate)
                : salesOrderItemRepository.getGrossProfit();

        List<Product> lowStockProducts = productRepository.findAll()
                .stream()
                .filter(product -> product.getStock() <= product.getMinStock())
                .toList();

        return new DashboardResponse(
                totalProducts,
                totalRevenue,
                grossProfit,
                lowStockProducts
        );
    }

    public List<MonthlySalesResponse> getMonthlySales(LocalDate startDate, LocalDate endDate) {
        boolean filtered = hasDateFilter(startDate, endDate);

        List<Object[]> rows = filtered
                ? salesOrderRepository.getMonthlySalesByDateRange(startDate, endDate)
                : salesOrderRepository.getMonthlySales();

        return rows.stream()
                .map(row -> new MonthlySalesResponse(
                        row[0].toString(),
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    private boolean hasDateFilter(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null;
    }
}