package com.knature.common.domain.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** 발주서 — 대기→승인→공장확인→생산중→출고→입고완료 (반려/취소 별도) */
@Entity
@Table(name = "purchase_orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder extends BaseEntity {

    public enum PoStatus {
        REQUESTED("승인대기"), APPROVED("승인"), CONFIRMED("공장확인"),
        IN_PRODUCTION("생산중"), SHIPPED("출고"), RECEIVED("입고완료"),
        REJECTED("반려"), CANCELLED("취소");

        private final String label;
        PoStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "images", "options", "category", "detailContent"})
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long unitCost = 0L;

    @Column(nullable = false)
    private Long totalCost = 0L;

    /** 납기 요청일 */
    @Column
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PoStatus status = PoStatus.REQUESTED;

    /** 누적 입고 수량 (부분입고 추적) */
    @Column(nullable = false)
    private Integer receivedQuantity = 0;

    @Column(length = 200)
    private String memo;

    /** 생성 주체 (관리자 아이디 또는 'auto') */
    @Column(nullable = false, length = 50)
    private String createdBy;

    @Builder
    public PurchaseOrder(String poNumber, Factory factory, Product product, Integer quantity,
                         Long unitCost, LocalDate dueDate, String memo, String createdBy) {
        this.poNumber = poNumber;
        this.factory = factory;
        this.product = product;
        this.quantity = quantity;
        this.unitCost = unitCost != null ? unitCost : 0L;
        this.totalCost = this.unitCost * quantity;
        this.dueDate = dueDate;
        this.memo = memo;
        this.createdBy = createdBy;
    }

    public boolean isOpen() {
        return status != PoStatus.RECEIVED && status != PoStatus.REJECTED && status != PoStatus.CANCELLED;
    }
}
