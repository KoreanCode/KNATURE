package com.knature.common.domain.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 입고 기록 — 발주 기반 (전량/부분), 검수(양품/불량) 후 양품만 본사 재고 반영 */
@Entity
@Table(name = "goods_receipts")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoodsReceipt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PurchaseOrder purchaseOrder;

    /** 입고 수량 (양품+불량) */
    @Column(nullable = false)
    private Integer receivedQuantity;

    /** 검수 양품 */
    @Column(nullable = false)
    private Integer goodQuantity;

    /** 검수 불량 */
    @Column(nullable = false)
    private Integer defectQuantity = 0;

    @Column(length = 200)
    private String defectReason;

    /** LOT 번호 (화장품 추적) */
    @Column(length = 50)
    private String lotNumber;

    @Column(nullable = false, length = 50)
    private String receivedBy;

    @Builder
    public GoodsReceipt(PurchaseOrder purchaseOrder, Integer receivedQuantity, Integer goodQuantity,
                        Integer defectQuantity, String defectReason, String lotNumber, String receivedBy) {
        this.purchaseOrder = purchaseOrder;
        this.receivedQuantity = receivedQuantity;
        this.goodQuantity = goodQuantity;
        this.defectQuantity = defectQuantity != null ? defectQuantity : 0;
        this.defectReason = defectReason;
        this.lotNumber = lotNumber;
        this.receivedBy = receivedBy;
    }
}
