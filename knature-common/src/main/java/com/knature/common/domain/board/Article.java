package com.knature.common.domain.board;

import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** 게시글 통합 엔티티 — 공지(NOTICE) / 이벤트·뉴스(EVENT) / FAQ */
@Entity
@Table(name = "articles")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    public enum ArticleType { NOTICE, EVENT, FAQ }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ArticleType type;

    /** 분류 (공지: 공지/안내, FAQ: 주문/배송/교환반품/상품/회원 등) */
    @Column(length = 50)
    private String category;

    /** 제목 (FAQ 는 질문) */
    @Column(nullable = false, length = 200)
    private String title;

    /** 본문 (FAQ 는 답변) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 대표 이미지 (이벤트용) */
    @Column(length = 500)
    private String imageUrl;

    /** 이벤트 기간 */
    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    /** 상단 고정 (공지) */
    @Column(nullable = false)
    private Boolean pinned = false;

    /** 노출 여부 */
    @Column(nullable = false)
    private Boolean visible = true;

    @Builder
    public Article(ArticleType type, String category, String title, String content,
                   String imageUrl, LocalDate startDate, LocalDate endDate, Boolean pinned) {
        this.type = type;
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pinned = pinned != null ? pinned : false;
    }
}
