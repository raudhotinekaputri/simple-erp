package com.example.simple_erp.controller;

import com.example.simple_erp.entity.DiscountSetting;
import com.example.simple_erp.service.DiscountSettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicDiscountSettingController {

    private final DiscountSettingService discountSettingService;

    public PublicDiscountSettingController(DiscountSettingService discountSettingService) {
        this.discountSettingService = discountSettingService;
    }

    @GetMapping("/discount-settings")
    public DiscountSetting getDiscountSetting() {
        return discountSettingService.getSetting();
    }
}