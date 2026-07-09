package com.knature.common.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String zipcode;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String addressDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberGrade grade = MemberGrade.NEW;

    @Column(nullable = false)
    private Long totalPurchaseAmount = 0L;

    /** 가용 적립금 (원) — 변동은 MileageHistory 로 추적 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long mileage = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    /** 구매 실적 기반 등급 자동 재계산 (승급/강등) */
    public void recalculateGrade() {
        long total = totalPurchaseAmount == null ? 0 : totalPurchaseAmount;
        MemberGrade newGrade = MemberGrade.NEW;
        for (MemberGrade g : MemberGrade.values()) {
            if (total >= g.getMinPurchase() && g.getMinPurchase() >= newGrade.getMinPurchase()) {
                newGrade = g;
            }
        }
        this.grade = newGrade;
    }

    @Builder
    public Member(String username, String password, String name, String email, String phone) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
