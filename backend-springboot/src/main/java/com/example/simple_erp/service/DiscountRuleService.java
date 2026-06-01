package com.example.simple_erp.service;

import com.example.simple_erp.dto.CreateDiscountRuleRequest;
import com.example.simple_erp.dto.UpdateDiscountRuleRequest;
import com.example.simple_erp.entity.DiscountRule;
import com.example.simple_erp.repository.DiscountRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DiscountRuleService {

    private final DiscountRuleRepository discountRuleRepository;

    public DiscountRuleService(DiscountRuleRepository discountRuleRepository) {
        this.discountRuleRepository = discountRuleRepository;
    }

    public List<DiscountRule> getAllDiscountRules() {
        return discountRuleRepository.findAllByOrderByStartDateDesc();
    }

    public DiscountRule createDiscountRule(CreateDiscountRuleRequest request) {
        validateDateRange(request.startDate(), request.endDate());

        DiscountRule discountRule = new DiscountRule(
                request.name(),
                request.minimumQuantity(),
                request.discountPercent(),
                request.startDate(),
                request.endDate()
        );

        return discountRuleRepository.save(discountRule);
    }

    public DiscountRule updateDiscountRule(Long id, UpdateDiscountRuleRequest request) {
        validateDateRange(request.startDate(), request.endDate());

        DiscountRule discountRule = discountRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount rule not found"));

        discountRule.setName(request.name());
        discountRule.setMinimumQuantity(request.minimumQuantity());
        discountRule.setDiscountPercent(request.discountPercent());
        discountRule.setStartDate(request.startDate());
        discountRule.setEndDate(request.endDate());

        return discountRuleRepository.save(discountRule);
    }

    public void deleteDiscountRule(Long id) {
        DiscountRule discountRule = discountRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount rule not found"));

        discountRuleRepository.delete(discountRule);
    }

    public BigDecimal getBestDiscountPercent(LocalDate orderDate, Integer quantity) {
        List<DiscountRule> applicableDiscounts =
                discountRuleRepository.findApplicableDiscounts(orderDate, quantity);

        if (applicableDiscounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return applicableDiscounts.get(0).getDiscountPercent();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }
    }
}