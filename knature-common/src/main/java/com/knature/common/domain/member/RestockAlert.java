package com.knature.common.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 재입고 알림 신청 — 품절 상품 재입고 시 자동 알림 처리 (3차) */
@Entity
@Table(name = "restock_alerts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"}))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestockAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    /** 재입고 알림 처리 여부/시각 (실 발송 채널 연동 전까지는 FO 알림 내역 표시로 대체) */
    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean notified = false;

    @Column
    private LocalDateTime notifiedAt;

    @Builder
    public RestockAlert(Member member, Product product) {
        this.member = member;
        this.product = product;
    }
}
