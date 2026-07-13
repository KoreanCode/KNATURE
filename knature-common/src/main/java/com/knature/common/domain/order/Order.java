package com.knature.common.domain.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    @Column(nullable = false, length = 50)
    private String ordererName;

    @Column(length = 100)
    private String ordererEmail;

    @Column(length = 20)
    private String ordererPhone;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(length = 20)
    private String receiverPhone;

    @Column(length = 10)
    private String receiverZipcode;

    @Column(length = 200)
    private String receiverAddress;

    @Column(length = 200)
    private String receiverAddressDetail;

    @Column(length = 200)
    private String deliveryMemo;

    @Column(nullable = false)
    private Long totalAmount = 0L;

    @Column(nullable = false)
    private Long deliveryFee = 0L;

    @Column(nullable = false)
    private Long paymentAmount = 0L;

    /** 사용한 적립금 (취소 시 환급) */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long usedMileage = 0L;

    /** 쿠폰 할인액 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long couponDiscount = 0L;

    /** 사용한 회원 쿠폰 id (취소 시 복구) */
    @Column
    private Long usedMemberCouponId;

    /** 사용한 예치금 (취소 시 환급) — 2차 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long usedDeposit = 0L;

    /** 비회원 주문 조회 비밀번호 (BCrypt 해시) — 2차 */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(length = 100)
    private String guestPassword;

    public boolean isGuest() {
        return member == null;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    /** 배송 처리 — 택배사 / 송장번호 (송장 입력 시 배송중 전환) */
    @Column(length = 50)
    private String courierCompany;

    @Column(length = 50)
    private String trackingNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"order", "hibernateLazyInitializer"})
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(String orderNumber, Member member, String ordererName, String ordererEmail,
                 String ordererPhone, String receiverName, String receiverPhone,
                 String receiverZipcode, String receiverAddress, String receiverAddressDetail,
                 String deliveryMemo, Long totalAmount, Long deliveryFee, Long paymentAmount,
                 PaymentMethod paymentMethod) {
        this.orderNumber = orderNumber;
        this.member = member;
        this.ordererName = ordererName;
        this.ordererEmail = ordererEmail;
        this.ordererPhone = ordererPhone;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverZipcode = receiverZipcode;
        this.receiverAddress = receiverAddress;
        this.receiverAddressDetail = receiverAddressDetail;
        this.deliveryMemo = deliveryMemo;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
    }
}
