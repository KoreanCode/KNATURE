package com.knature.common.domain.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

/** 상품 후기 — 배송완료 주문 상품에 대해 작성, 작성 시 리뷰 적립금 지급 */
@Entity
@Table(name = "reviews")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "images", "options", "category", "detailContent"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "email", "phone", "zipcode", "address", "addressDetail", "totalPurchaseAmount", "mileage"})
    private Member member;

    /** 별점 1~5 */
    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 블라인드 (부적절 후기 — FO 미노출) */
    @Column(nullable = false)
    private Boolean blinded = false;

    @Builder
    public Review(Product product, Member member, Integer rating, String content) {
        this.product = product;
        this.member = member;
        this.rating = rating;
        this.content = content;
    }
}
