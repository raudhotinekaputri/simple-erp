package com.example.simple_erp.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
        @NotBlank(message = "Customer name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Address is required")
        String address
) {
}