package com.knature.common.domain.coupon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 회원에게 발급된 쿠폰 */
@Entity
@Table(name = "member_coupons")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Coupon coupon;

    @Column(nullable = false)
    private Boolean used = false;

    @Column
    private LocalDateTime usedAt;

    @Builder
    public MemberCoupon(Member member, Coupon coupon) {
        this.member = member;
        this.coupon = coupon;
    }

    public void use() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    public void restore() {
        this.used = false;
        this.usedAt = null;
    }
}
