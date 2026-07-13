package com.knature.common.domain.deposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

/** 예치금 변동 이력 (+지급·환불 / -사용·차감) — 2차 */
@Entity
@Table(name = "deposit_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositHistory extends BaseEntity {

    public enum DepositType { ADMIN, USE, REFUND }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    /** 변동액 (+지급 / -사용) */
    @Column(nullable = false)
    private Long amount;

    /** 변동 후 잔액 */
    @Column(nullable = false)
    private Long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DepositType depositType;

    @Column(nullable = false, length = 200)
    private String reason;

    @Builder
    public DepositHistory(Member member, Long amount, Long balanceAfter, DepositType depositType, String reason) {
        this.member = member;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.depositType = depositType;
        this.reason = reason;
    }
}
