package com.example.simple_erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateSalesOrderStatusRequest(
        @NotNull(message = "Order date is required")
        LocalDate orderDate,

        String trackingNumber,

        @NotBlank(message = "Shipping status is required")
        String shippingStatus,

        @NotBlank(message = "Payment status is required")
        String paymentStatus,

        @NotBlank(message = "Order status is required")
        String orderStatus,

        @NotBlank(message = "Refund status is required")
        String refundStatus
) {
}