package com.knature.common.domain.scm;

import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 공장(공급사) — 상품 생산처 */
@Entity
@Table(name = "factories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Factory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String bizNumber;

    @Column(length = 200)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String managerName;

    /** 기본 생산 리드타임 (일) */
    @Column(nullable = false)
    private Integer leadTimeDays = 7;

    /** 담당 지역 (쉼표 구분 키워드, 예: "서울,경기,인천") — 주문 배송지 기반 자동발주 공장 선택에 사용 */
    @Column(length = 200)
    private String region;

    @Column(nullable = false)
    private Boolean active = true;

    @Builder
    public Factory(String name, String bizNumber, String address, String phone,
                   String managerName, Integer leadTimeDays, String region) {
        this.name = name;
        this.bizNumber = bizNumber;
        this.address = address;
        this.phone = phone;
        this.managerName = managerName;
        this.leadTimeDays = leadTimeDays != null ? leadTimeDays : 7;
        this.region = region;
    }

    /** 배송지 주소가 담당 지역에 해당하는지 */
    public boolean coversAddress(String address) {
        if (region == null || region.isBlank() || address == null) return false;
        for (String keyword : region.split(",")) {
            if (!keyword.isBlank() && address.contains(keyword.trim())) return true;
        }
        return false;
    }
}
