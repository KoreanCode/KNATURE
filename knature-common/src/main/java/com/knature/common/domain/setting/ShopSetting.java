package com.knature.common.domain.setting;

import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 쇼핑몰 설정 (key-value) — 상점 정보 / 배송 정책 등 */
@Entity
@Table(name = "shop_settings")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    @Builder
    public ShopSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }
}
