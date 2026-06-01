package com.example.simple_erp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSalesOrderRequest(
        @NotNull(message = "Order date is required")
        LocalDate orderDate,

        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Address is required")
        String address,

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @NotBlank(message = "Shipping method is required")
        String shippingMethod,

        String trackingNumber,

        @NotBlank(message = "Shipping status is required")
        String shippingStatus,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotBlank(message = "Payment status is required")
        String paymentStatus
) {
}