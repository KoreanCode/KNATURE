package com.knature.fo.controller;

import com.knature.common.domain.board.Article.ArticleType;
import com.knature.common.domain.board.Inquiry;
import com.knature.common.domain.board.Review;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.mileage.MileageHistory.MileageType;
import com.knature.common.domain.product.Product;
import com.knature.common.repository.*;
import com.knature.common.service.MileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** FO 커뮤니티 — 게시판 조회(공개) / 상품후기 조회·작성 / 1:1 문의 */
@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final ArticleRepository articleRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopSettingRepository shopSettingRepository;
    private final MileageService mileageService;

    private Member me(Authentication auth) {
        return memberRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    // ===== 공개 =====

    @GetMapping("/api/articles")
    public ResponseEntity<?> articles(@RequestParam ArticleType type) {
        return ResponseEntity.ok(articleRepository.findByTypeAndVisibleTrueOrderByPinnedDescIdDesc(type));
    }

    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<?> productReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdAndBlindedFalseOrderByIdDesc(productId));
    }

    // ===== 회원 =====

    /** 후기 작성 — 배송완료 구매자만, 상품당 1회, 작성 시 리뷰 적립금 지급 */
    @PostMapping("/api/reviews")
    @Transactional
    public ResponseEntity<?> writeReview(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        Long productId = Long.parseLong(body.get("productId"));
        int rating = Integer.parseInt(body.getOrDefault("rating", "5"));
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("후기 내용을 입력해주세요.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1~5점으로 선택해주세요.");
        }
        if (orderItemRepository.countDeliveredPurchase(member.getId(), productId) == 0) {
            throw new IllegalArgumentException("배송완료된 구매 상품에만 후기를 작성할 수 있습니다.");
        }
        if (reviewRepository.existsByMemberIdAndProductId(member.getId(), productId)) {
            throw new IllegalArgumentException("이미 이 상품에 후기를 작성하셨습니다.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        reviewRepository.save(Review.builder()
                .product(product).member(member).rating(rating).content(content).build());

        // 리뷰 적립금 (설정 mileage.reviewBonus, 기본 500P)
        long bonus = shopSettingRepository.findBySettingKey("mileage.reviewBonus")
                .map(s -> { try { return Long.parseLong(s.getSettingValue().trim()); } catch (Exception e) { return 500L; } })
                .orElse(500L);
        if (bonus > 0) {
            mileageService.change(member, bonus, MileageType.REVIEW, "후기 작성 적립 (" + product.getName() + ")");
        }
        return ResponseEntity.ok(Map.of("message", "후기가 등록되었습니다." + (bonus > 0 ? " (적립금 " + bonus + "P 지급)" : "")));
    }

    @GetMapping("/api/inquiries")
    public ResponseEntity<?> myInquiries(Authentication auth) {
        return ResponseEntity.ok(inquiryRepository.findByMemberIdOrderByIdDesc(me(auth).getId()));
    }

    @PostMapping("/api/inquiries")
    public ResponseEntity<?> writeInquiry(@RequestBody Map<String, String> body, Authentication auth) {
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("제목과 내용을 입력해주세요.");
        }
        Inquiry inquiry = Inquiry.builder()
                .member(me(auth))
                .category(body.getOrDefault("category", "기타"))
                .title(title)
                .content(content)
                .build();
        inquiryRepository.save(inquiry);
        return ResponseEntity.ok(Map.of("message", "문의가 접수되었습니다. 답변은 마이페이지에서 확인하실 수 있습니다."));
    }
}
