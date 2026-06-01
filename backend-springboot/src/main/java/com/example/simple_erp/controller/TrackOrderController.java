package com.example.simple_erp.controller;

import com.example.simple_erp.entity.SalesOrder;
import com.example.simple_erp.service.SalesOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/orders")
public class TrackOrderController {

    private final SalesOrderService salesOrderService;

    public TrackOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping("/track")
    public SalesOrder trackOrder(
            @RequestParam Long orderId,
            @RequestParam String phone
    ) {
        return salesOrderService.trackOrder(orderId, phone);
    }
}