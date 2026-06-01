package com.example.simple_erp.controller;

import com.example.simple_erp.dto.UpdateDiscountSettingRequest;
import com.example.simple_erp.entity.DiscountSetting;
import com.example.simple_erp.service.DiscountSettingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discount-settings")
public class DiscountSettingController {

    private final DiscountSettingService discountSettingService;

    public DiscountSettingController(DiscountSettingService discountSettingService) {
        this.discountSettingService = discountSettingService;
    }

    @GetMapping
    public DiscountSetting getDiscountSetting() {
        return discountSettingService.getSetting();
    }

    @PutMapping
    public DiscountSetting updateDiscountSetting(
            @Valid @RequestBody UpdateDiscountSettingRequest request
    ) {
        return discountSettingService.updateSetting(request);
    }

    @DeleteMapping
    public DiscountSetting deleteCurrentDiscount() {
        return discountSettingService.deleteCurrentDiscount();
    }
}