package com.knature.common.domain.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

/** 공장-상품 매핑 — 어떤 상품을 어느 공장에서 생산하는지 (자동 발주의 기준 데이터) */
@Entity
@Table(name = "factory_products", uniqueConstraints = @UniqueConstraint(columnNames = {"factory_id", "product_id"}))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FactoryProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "images", "options", "category", "detailContent"})
    private Product product;

    /** 생산 단가 (원) */
    @Column(nullable = false)
    private Long unitCost = 0L;

    /** 최소 발주 수량 (MOQ) */
    @Column(nullable = false)
    private Integer moq = 1;

    /** 이 상품의 생산 리드타임 (일, null 이면 공장 기본값) */
    @Column
    private Integer leadTimeDays;

    @Builder
    public FactoryProduct(Factory factory, Product product, Long unitCost, Integer moq, Integer leadTimeDays) {
        this.factory = factory;
        this.product = product;
        this.unitCost = unitCost != null ? unitCost : 0L;
        this.moq = moq != null ? moq : 1;
        this.leadTimeDays = leadTimeDays;
    }
}
