package com.knature.fo.controller;

import com.knature.common.repository.ShopSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 상점 공개 정보 — 푸터 회사정보 + 배송 정책 (BO 설정 연동) */
@RestController
@RequiredArgsConstructor
public class ShopInfoController {

    /** 공개 허용 키만 노출 (설정 전체 노출 금지) */
    private static final Set<String> PUBLIC_KEYS = Set.of(
            "shop.name", "shop.ceo", "shop.bizNumber", "shop.address", "shop.tel", "shop.email",
            "delivery.baseFee", "delivery.freeThreshold", "delivery.remoteAreaFee"
    );

    private final ShopSettingRepository shopSettingRepository;

    @GetMapping("/api/shop-info")
    public ResponseEntity<?> shopInfo() {
        Map<String, String> result = new LinkedHashMap<>();
        shopSettingRepository.findAll().forEach(s -> {
            if (PUBLIC_KEYS.contains(s.getSettingKey())) {
                result.put(s.getSettingKey(), s.getSettingValue());
            }
        });
        return ResponseEntity.ok(result);
    }
}
