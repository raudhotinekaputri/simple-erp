package com.example.simple_erp.repository;

import com.example.simple_erp.entity.DiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DiscountRuleRepository extends JpaRepository<DiscountRule, Long> {

    List<DiscountRule> findAllByOrderByStartDateDesc();

    @Query("""
        SELECT d
        FROM DiscountRule d
        WHERE d.startDate <= :orderDate
        AND d.endDate >= :orderDate
        AND d.minimumQuantity <= :quantity
        ORDER BY d.discountPercent DESC
    """)
    List<DiscountRule> findApplicableDiscounts(LocalDate orderDate, Integer quantity);
}