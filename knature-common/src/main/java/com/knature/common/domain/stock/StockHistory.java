package com.knature.common.domain.stock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

/** 재고 수동 조정 이력 (1차: 수동 관리, 입고/출고 자동 반영은 2차) */
@Entity
@Table(name = "stock_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "images", "options", "category"})
    private Product product;

    @Column(nullable = false)
    private Integer beforeQuantity;

    @Column(nullable = false)
    private Integer afterQuantity;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(nullable = false, length = 50)
    private String adjustedBy;

    @Builder
    public StockHistory(Product product, Integer beforeQuantity, Integer afterQuantity,
                        String reason, String adjustedBy) {
        this.product = product;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.reason = reason;
        this.adjustedBy = adjustedBy;
    }
}
