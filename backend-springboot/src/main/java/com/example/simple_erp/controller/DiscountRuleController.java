package com.example.simple_erp.controller;

import com.example.simple_erp.dto.ApiResponse;
import com.example.simple_erp.dto.CreateDiscountRuleRequest;
import com.example.simple_erp.dto.UpdateDiscountRuleRequest;
import com.example.simple_erp.entity.DiscountRule;
import com.example.simple_erp.service.DiscountRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discount-rules")
public class DiscountRuleController {

    private final DiscountRuleService discountRuleService;

    public DiscountRuleController(DiscountRuleService discountRuleService) {
        this.discountRuleService = discountRuleService;
    }

    @GetMapping
    public List<DiscountRule> getAllDiscountRules() {
        return discountRuleService.getAllDiscountRules();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiscountRule createDiscountRule(
            @Valid @RequestBody CreateDiscountRuleRequest request
    ) {
        return discountRuleService.createDiscountRule(request);
    }

    @PutMapping("/{id}")
    public DiscountRule updateDiscountRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiscountRuleRequest request
    ) {
        return discountRuleService.updateDiscountRule(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteDiscountRule(@PathVariable Long id) {
        discountRuleService.deleteDiscountRule(id);
        return new ApiResponse("Discount rule deleted successfully");
    }
}