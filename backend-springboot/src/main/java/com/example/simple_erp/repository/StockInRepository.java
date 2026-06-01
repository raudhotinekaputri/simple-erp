package com.example.simple_erp.repository;

import com.example.simple_erp.entity.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockInRepository extends JpaRepository<StockIn, Long> {

    @Modifying
    @Query(
            value = "DELETE FROM stock_in WHERE product_id = :productId",
            nativeQuery = true
    )
    int deleteByProductIdNative(@Param("productId") Long productId);
}