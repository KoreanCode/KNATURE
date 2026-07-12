package com.knature.common.domain.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 1:1 문의 — FO 작성 → BO 답변 */
@Entity
@Table(name = "inquiries")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "email", "phone", "zipcode", "address", "addressDetail", "totalPurchaseAmount", "mileage"})
    private Member member;

    /** 분류: 상품/주문/배송/기타 */
    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column
    private LocalDateTime answeredAt;

    @Column(length = 50)
    private String answeredBy;

    @Builder
    public Inquiry(Member member, String category, String title, String content) {
        this.member = member;
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public boolean isAnswered() {
        return answer != null && !answer.isBlank();
    }
}
