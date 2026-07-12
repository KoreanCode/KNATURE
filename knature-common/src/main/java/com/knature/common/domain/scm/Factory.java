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

    @Column(nullable = false)
    private Boolean active = true;

    @Builder
    public Factory(String name, String bizNumber, String address, String phone,
                   String managerName, Integer leadTimeDays) {
        this.name = name;
        this.bizNumber = bizNumber;
        this.address = address;
        this.phone = phone;
        this.managerName = managerName;
        this.leadTimeDays = leadTimeDays != null ? leadTimeDays : 7;
    }
}
