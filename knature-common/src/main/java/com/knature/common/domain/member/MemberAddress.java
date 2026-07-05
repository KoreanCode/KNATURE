package com.knature.common.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 회원 배송지 (마이페이지 배송지 관리 / 주문서 배송지 선택) */
@Entity
@Table(name = "member_addresses")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private Member member;

    @Column(nullable = false, length = 50)
    private String alias; // 배송지명 (예: 집, 회사)

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 200)
    private String addressDetail;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Builder
    public MemberAddress(Member member, String alias, String receiverName, String receiverPhone,
                         String zipcode, String address, String addressDetail, Boolean isDefault) {
        this.member = member;
        this.alias = alias;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault != null ? isDefault : false;
    }
}
