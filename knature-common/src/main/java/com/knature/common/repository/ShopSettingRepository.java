package com.knature.common.repository;

import com.knature.common.domain.setting.ShopSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopSettingRepository extends JpaRepository<ShopSetting, Long> {
    Optional<ShopSetting> findBySettingKey(String settingKey);
}
