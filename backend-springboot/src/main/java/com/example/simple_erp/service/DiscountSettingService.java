package com.example.simple_erp.service;

import com.example.simple_erp.dto.UpdateDiscountSettingRequest;
import com.example.simple_erp.entity.DiscountSetting;
import com.example.simple_erp.repository.DiscountSettingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DiscountSettingService {

    private final DiscountSettingRepository discountSettingRepository;

    public DiscountSettingService(DiscountSettingRepository discountSettingRepository) {
        this.discountSettingRepository = discountSettingRepository;
    }

    public DiscountSetting getSetting() {
        return discountSettingRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> discountSettingRepository.save(
                        new DiscountSetting(
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        )
                ));
    }

    public DiscountSetting updateSetting(UpdateDiscountSettingRequest request) {
        DiscountSetting setting = getSetting();

        setting.setOneBoxDiscountPercent(request.oneBoxDiscountPercent());
        setting.setThreeBoxDiscountPercent(request.threeBoxDiscountPercent());

        return discountSettingRepository.save(setting);
    }

    public DiscountSetting deleteCurrentDiscount() {
        DiscountSetting setting = getSetting();

        setting.setOneBoxDiscountPercent(BigDecimal.ZERO);
        setting.setThreeBoxDiscountPercent(BigDecimal.ZERO);

        return discountSettingRepository.save(setting);
    }
}