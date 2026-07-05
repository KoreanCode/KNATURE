package com.knature.common.domain.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 주문 상태 변경 이력 — 대시보드 '오늘 처리한 일' 집계 및 감사 추적용 */
@Entity
@Table(name = "order_status_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "member"})
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus toStatus;

    @Column(nullable = false, length = 50)
    private String changedBy;

    @Builder
    public OrderStatusHistory(Order order, OrderStatus fromStatus, OrderStatus toStatus, String changedBy) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
    }
}
