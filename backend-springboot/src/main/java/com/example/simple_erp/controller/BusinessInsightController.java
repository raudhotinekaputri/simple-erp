package com.example.simple_erp.controller;

import com.example.simple_erp.dto.BusinessInsightResponse;
import com.example.simple_erp.service.BusinessInsightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard/business-insight")
public class BusinessInsightController {

    private final BusinessInsightService businessInsightService;

    public BusinessInsightController(BusinessInsightService businessInsightService) {
        this.businessInsightService = businessInsightService;
    }

    @GetMapping
    public BusinessInsightResponse getBusinessInsight(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return businessInsightService.getBusinessInsight(startDate, endDate);
    }
}