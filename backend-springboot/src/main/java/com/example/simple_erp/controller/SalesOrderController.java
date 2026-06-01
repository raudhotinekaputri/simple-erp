package com.example.simple_erp.controller;

import com.example.simple_erp.dto.ApiResponse;
import com.example.simple_erp.dto.CreateSalesOrderRequest;
import com.example.simple_erp.dto.UpdateSalesOrderStatusRequest;
import com.example.simple_erp.entity.SalesOrder;
import com.example.simple_erp.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrder createSalesOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
        return salesOrderService.createSalesOrder(request);
    }

    @GetMapping
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderService.getAllSalesOrders();
    }

    @PutMapping("/{id}/status")
    public SalesOrder updateSalesOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSalesOrderStatusRequest request
    ) {
        return salesOrderService.updateSalesOrderStatus(id, request);
    }

    @PutMapping("/{id}/cancel")
    public SalesOrder cancelSalesOrder(@PathVariable Long id) {
        return salesOrderService.cancelSalesOrder(id);
    }

    @PutMapping("/{id}/refund")
    public SalesOrder requestRefund(@PathVariable Long id) {
        return salesOrderService.requestRefund(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return new ApiResponse("Sales order deleted successfully");
    }
}