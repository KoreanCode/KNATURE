package com.knature.bo.controller;

import com.knature.common.domain.board.Review;
import com.knature.common.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** BO 상품후기 관리 — 목록(상품별 필터) / 블라인드 토글 / 삭제 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewAdminController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(productId != null
                ? reviewRepository.findByProductIdOrderByIdDesc(productId)
                : reviewRepository.findAllByOrderByIdDesc());
    }

    /** 블라인드 토글 — 부적절 후기 FO 미노출 처리 */
    @PatchMapping("/{id}/blind")
    @Transactional
    public ResponseEntity<?> toggleBlind(@PathVariable Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));
        review.setBlinded(!Boolean.TRUE.equals(review.getBlinded()));
        return ResponseEntity.ok(Map.of("message",
                Boolean.TRUE.equals(review.getBlinded()) ? "블라인드 처리되었습니다." : "블라인드가 해제되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "후기가 삭제되었습니다."));
    }
}
