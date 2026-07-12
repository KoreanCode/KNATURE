package com.knature.common.domain.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

/** 공장별 보유 재고 — 공장관리자가 자기 공장 수량 입력/수정, 입고(출고통보) 시 자동 차감 */
@Entity
@Table(name = "factory_stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"factory_id", "product_id"}))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FactoryStock extends BaseEntity {

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

    @Column(nullable = false)
    private Integer quantity = 0;

    @Builder
    public FactoryStock(Factory factory, Product product, Integer quantity) {
        this.factory = factory;
        this.product = product;
        this.quantity = quantity != null ? quantity : 0;
    }
}
