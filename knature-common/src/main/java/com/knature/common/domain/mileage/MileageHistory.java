package com.knature.common.domain.mileage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

/** 적립금 변동 이력 (+적립 / -사용·차감) */
@Entity
@Table(name = "mileage_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MileageHistory extends BaseEntity {

    public enum MileageType { JOIN, PURCHASE, USE, ADMIN, REFUND, REVIEW }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    /** 변동액 (+적립 / -사용) */
    @Column(nullable = false)
    private Long amount;

    /** 변동 후 잔액 */
    @Column(nullable = false)
    private Long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MileageType mileageType;

    @Column(nullable = false, length = 200)
    private String reason;

    @Builder
    public MileageHistory(Member member, Long amount, Long balanceAfter, MileageType mileageType, String reason) {
        this.member = member;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.mileageType = mileageType;
        this.reason = reason;
    }
}
