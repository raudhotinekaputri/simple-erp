package com.example.simple_erp.repository;

import com.example.simple_erp.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    @Query("""
        SELECT COALESCE(SUM(i.subtotal - (p.costPrice * i.quantity)), 0)
        FROM SalesOrderItem i
        JOIN i.product p
        JOIN i.salesOrder s
        WHERE s.orderStatus IS NULL
        OR LOWER(TRIM(s.orderStatus)) <> 'cancelled'
    """)
    BigDecimal getGrossProfit();

    @Query("""
        SELECT COALESCE(SUM(i.subtotal - (p.costPrice * i.quantity)), 0)
        FROM SalesOrderItem i
        JOIN i.product p
        JOIN i.salesOrder s
        WHERE (s.orderStatus IS NULL OR LOWER(TRIM(s.orderStatus)) <> 'cancelled')
        AND s.orderDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal getGrossProfitByDateRange(LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query(
            value = "DELETE FROM sales_order_items WHERE sales_order_id = :salesOrderId",
            nativeQuery = true
    )
    int deleteItemsBySalesOrderId(@Param("salesOrderId") Long salesOrderId);

    @Modifying
    @Query(
            value = "DELETE FROM sales_order_items WHERE product_id = :productId",
            nativeQuery = true
    )
    int deleteItemsByProductId(@Param("productId") Long productId);
}