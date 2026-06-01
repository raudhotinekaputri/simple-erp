package com.example.simple_erp.controller;

import com.example.simple_erp.dto.ApiResponse;
import com.example.simple_erp.dto.StockInRequest;
import com.example.simple_erp.service.StockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-in")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ApiResponse addStock(@Valid @RequestBody StockInRequest request) {
        stockService.addStock(request);
        return new ApiResponse("Stock added successfully");
    }
}