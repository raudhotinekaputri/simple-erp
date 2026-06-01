package com.example.simple_erp.repository;

import com.example.simple_erp.entity.DiscountSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountSettingRepository extends JpaRepository<DiscountSetting, Long> {
}