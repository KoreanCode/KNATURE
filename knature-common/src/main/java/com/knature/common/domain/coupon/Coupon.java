package com.knature.common.domain.coupon;

import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** 쿠폰 정의 (발급은 MemberCoupon) */
@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    public enum DiscountType { FIXED, PERCENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 자동발급 식별용 코드 (예: WELCOME) */
    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DiscountType discountType;

    /** FIXED: 할인액(원) / PERCENT: 할인율(%) */
    @Column(nullable = false)
    private Long amount;

    /** 정률 할인 상한액 (null = 무제한) */
    @Column
    private Long maxDiscount;

    /** 최소 주문 금액 (사용 조건) */
    @Column(nullable = false)
    private Long minOrderAmount = 0L;

    /** 유효기간 (이 날짜까지 사용 가능, null = 무기한) */
    @Column
    private LocalDate validUntil;

    @Column(nullable = false)
    private Boolean active = true;

    @Builder
    public Coupon(String code, String name, DiscountType discountType, Long amount,
                  Long maxDiscount, Long minOrderAmount, LocalDate validUntil) {
        this.code = code;
        this.name = name;
        this.discountType = discountType;
        this.amount = amount;
        this.maxDiscount = maxDiscount;
        this.minOrderAmount = minOrderAmount != null ? minOrderAmount : 0L;
        this.validUntil = validUntil;
    }

    /** 주문 금액에 대한 할인액 계산 */
    public long calculateDiscount(long orderAmount) {
        long discount = discountType == DiscountType.PERCENT
                ? orderAmount * amount / 100
                : amount;
        if (maxDiscount != null && discount > maxDiscount) discount = maxDiscount;
        return Math.min(discount, orderAmount);
    }
}
