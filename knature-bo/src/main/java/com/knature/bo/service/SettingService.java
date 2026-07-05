package com.knature.bo.service;

import com.knature.common.domain.setting.ShopSetting;
import com.knature.common.repository.ShopSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final ShopSettingRepository shopSettingRepository;

    public Map<String, String> getSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        shopSettingRepository.findAll().forEach(s -> map.put(s.getSettingKey(), s.getSettingValue()));
        return map;
    }

    @Transactional
    public void saveSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            ShopSetting setting = shopSettingRepository.findBySettingKey(key)
                    .orElseGet(() -> ShopSetting.builder().settingKey(key).build());
            setting.setSettingValue(value);
            shopSettingRepository.save(setting);
        });
    }
}
