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
}
