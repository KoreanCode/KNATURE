package com.knature.common.domain.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"children", "parent", "hibernateLazyInitializer"})
    private ProductCategory category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private Long price = 0L;

    private Long salePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String detailContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ON_SALE;

    @Column(nullable = false)
    private Boolean displayed = true;

    /** 본사(판매) 재고 수량 — 1차 재고관리는 상품 단위 수동 관리 */
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer stockQuantity = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @JsonIgnoreProperties({"product", "hibernateLazyInitializer"})
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @JsonIgnoreProperties({"product", "hibernateLazyInitializer"})
    private List<ProductOption> options = new ArrayList<>();

    @Builder
    public Product(ProductCategory category, String name, String code, Long price, Long salePrice,
                   String description, String detailContent) {
        this.category = category;
        this.name = name;
        this.code = code;
        this.price = price;
        this.salePrice = salePrice;
        this.description = description;
        this.detailContent = detailContent;
    }

    public Long getDisplayPrice() {
        return salePrice != null && salePrice > 0 ? salePrice : price;
    }

    public int getDiscountRate() {
        if (salePrice == null || salePrice <= 0 || price <= 0) return 0;
        return (int) ((price - salePrice) * 100 / price);
    }

    /**
     * 재고-판매상태 자동 규칙 (FO SOLD 배지 연동)
     * - 재고 0 → 판매중이면 품절 처리
     * - 재고 확보 → 품절이면 판매중 복원
     * - 숨김(HIDDEN)은 관리자 의도이므로 유지
     * 상품 저장/상태변경/재고조정 등 모든 변경 경로에서 호출한다.
     */
    public void applyStockStatusRule() {
        int qty = stockQuantity == null ? 0 : stockQuantity;
        if (qty == 0 && status == ProductStatus.ON_SALE) {
            status = ProductStatus.SOLD_OUT;
        } else if (qty > 0 && status == ProductStatus.SOLD_OUT) {
            status = ProductStatus.ON_SALE;
        }
    }
}
