package com.example.simple_erp.repository;

import com.example.simple_erp.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByIdAndPhone(Long id, String phone);

    Long countByPaymentStatus(String paymentStatus);

    Long countByOrderStatus(String orderStatus);

    Long countByRefundStatus(String refundStatus);

    @Query("""
        SELECT COALESCE(SUM(s.totalAmount), 0)
        FROM SalesOrder s
    """)
    BigDecimal getTotalRevenue();

    @Query("""
        SELECT COALESCE(SUM(s.totalAmount), 0)
        FROM SalesOrder s
        WHERE s.orderDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate);

    @Query(value = """
        SELECT 
            TO_CHAR(order_date, 'YYYY-MM') AS month,
            COALESCE(SUM(total_amount), 0) AS total_sales
        FROM sales_orders
        GROUP BY TO_CHAR(order_date, 'YYYY-MM')
        ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getMonthlySales();

    @Query(value = """
        SELECT 
            TO_CHAR(order_date, 'YYYY-MM') AS month,
            COALESCE(SUM(total_amount), 0) AS total_sales
        FROM sales_orders
        WHERE order_date BETWEEN :startDate AND :endDate
        GROUP BY TO_CHAR(order_date, 'YYYY-MM')
        ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getMonthlySalesByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE LOWER(TRIM(s.paymentStatus)) = 'unpaid'
        AND (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
    """)
    Long countPendingPaymentOrders();

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE LOWER(TRIM(s.paymentStatus)) = 'unpaid'
        AND (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
        AND s.orderDate BETWEEN :startDate AND :endDate
    """)
    Long countPendingPaymentOrdersByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE LOWER(TRIM(s.paymentStatus)) = 'unpaid'
        AND (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
        AND s.orderDate <= :thresholdDate
    """)
    Long countPaymentRiskOrders(LocalDate thresholdDate);

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE LOWER(TRIM(s.paymentStatus)) = 'unpaid'
        AND (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
        AND s.orderDate <= :thresholdDate
        AND s.orderDate BETWEEN :startDate AND :endDate
    """)
    Long countPaymentRiskOrdersByDateRange(
            LocalDate thresholdDate,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
        AND s.orderDate <= :thresholdDate
        AND (
            s.shippingStatus IS NULL
            OR LOWER(TRIM(s.shippingStatus)) NOT IN ('delivered', 'returned', 'cancelled')
        )
    """)
    Long countDelayedShipmentOrders(LocalDate thresholdDate);

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
        AND s.orderDate <= :thresholdDate
        AND s.orderDate BETWEEN :startDate AND :endDate
        AND (
            s.shippingStatus IS NULL
            OR LOWER(TRIM(s.shippingStatus)) NOT IN ('delivered', 'returned', 'cancelled')
        )
    """)
    Long countDelayedShipmentOrdersByDateRange(
            LocalDate thresholdDate,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE s.orderDate BETWEEN :startDate AND :endDate
    """)
    Long countOrdersByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE LOWER(TRIM(s.orderStatus)) = LOWER(TRIM(:orderStatus))
        AND s.orderDate BETWEEN :startDate AND :endDate
    """)
    Long countByOrderStatusAndDateRange(
            String orderStatus,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE LOWER(TRIM(s.refundStatus)) = LOWER(TRIM(:refundStatus))
        AND s.orderDate BETWEEN :startDate AND :endDate
    """)
    Long countByRefundStatusAndDateRange(
            String refundStatus,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE s.discountAmount > 0
    """)
    Long countOrdersWithDiscount();

    @Query("""
        SELECT COUNT(s)
        FROM SalesOrder s
        WHERE s.discountAmount > 0
        AND s.orderDate BETWEEN :startDate AND :endDate
    """)
    Long countOrdersWithDiscountByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT s.productName, COALESCE(SUM(s.totalAmount), 0)
        FROM SalesOrder s
        GROUP BY s.productName
        ORDER BY COALESCE(SUM(s.totalAmount), 0) DESC
    """)
    List<Object[]> getRevenueByProduct();

    @Query("""
        SELECT s.productName, COALESCE(SUM(s.totalAmount), 0)
        FROM SalesOrder s
        WHERE s.orderDate BETWEEN :startDate AND :endDate
        GROUP BY s.productName
        ORDER BY COALESCE(SUM(s.totalAmount), 0) DESC
    """)
    List<Object[]> getRevenueByProductByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT s.productName, COALESCE(SUM(s.quantity), 0)
        FROM SalesOrder s
        GROUP BY s.productName
        ORDER BY COALESCE(SUM(s.quantity), 0) DESC
    """)
    List<Object[]> getBestSellingProducts();

    @Query("""
        SELECT s.productName, COALESCE(SUM(s.quantity), 0)
        FROM SalesOrder s
        WHERE s.orderDate BETWEEN :startDate AND :endDate
        GROUP BY s.productName
        ORDER BY COALESCE(SUM(s.quantity), 0) DESC
    """)
    List<Object[]> getBestSellingProductsByDateRange(LocalDate startDate, LocalDate endDate);
}