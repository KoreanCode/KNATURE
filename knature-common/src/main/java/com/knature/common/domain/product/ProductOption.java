package com.knature.common.domain.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_options")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // 역참조 직렬화 방지 (순환·LAZY 프록시)
    private Product product;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long additionalPrice = 0L;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public ProductOption(Product product, String name, Long additionalPrice, Integer stockQuantity, Integer sortOrder) {
        this.product = product;
        this.name = name;
        this.additionalPrice = additionalPrice != null ? additionalPrice : 0L;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
