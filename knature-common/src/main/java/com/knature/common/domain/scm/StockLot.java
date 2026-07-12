package com.knature.common.domain.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** LOT/유통기한 — 화장품 로트 추적 (입고 시 생성) */
@Entity
@Table(name = "stock_lots")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockLot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "images", "options", "category", "detailContent"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Factory factory;

    @Column(nullable = false, length = 50)
    private String lotNumber;

    @Column
    private LocalDate manufactureDate;

    @Column
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public StockLot(Product product, Factory factory, String lotNumber,
                    LocalDate manufactureDate, LocalDate expiryDate, Integer quantity) {
        this.product = product;
        this.factory = factory;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
    }
}
