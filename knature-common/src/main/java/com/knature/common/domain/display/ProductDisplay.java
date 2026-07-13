package com.knature.common.domain.display;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

/** 메인 진열 — MD CHOICE / MONTH BEST 섹션별 상품 배치 (2차) */
@Entity
@Table(name = "product_displays",
        uniqueConstraints = @UniqueConstraint(columnNames = {"section", "product_id"}))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDisplay extends BaseEntity {

    public enum Section {
        MD_CHOICE("MD CHOICE"),
        MONTH_BEST("MONTH BEST");

        private final String label;
        Section(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public ProductDisplay(Section section, Product product, Integer sortOrder) {
        this.section = section;
        this.product = product;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }
}
