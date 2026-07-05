package com.knature.common.domain.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // 역참조 직렬화 방지 (순환·LAZY 프록시)
    private Product product;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isMain = false;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public ProductImage(Product product, String imageUrl, Boolean isMain, Integer sortOrder) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.isMain = isMain != null ? isMain : false;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
