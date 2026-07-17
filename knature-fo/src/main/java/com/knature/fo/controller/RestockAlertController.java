package com.knature.fo.controller;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.RestockAlert;
import com.knature.common.domain.product.Product;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.ProductRepository;
import com.knature.common.repository.RestockAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** FO 재입고 알림 신청 — 품절 상품 대상 (3차) */
@RestController
@RequestMapping("/api/restock-alerts")
@RequiredArgsConstructor
public class RestockAlertController {

    private final RestockAlertRepository alertRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    private Member me(Authentication auth) {
        return memberRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        return ResponseEntity.ok(alertRepository.findByMemberIdOrderByIdDesc(me(auth).getId()));
    }

    /** 특정 상품 신청 여부 */
    @GetMapping("/check/{productId}")
    public ResponseEntity<?> check(@PathVariable Long productId, Authentication auth) {
        var existing = alertRepository.findByMemberIdAndProductId(me(auth).getId(), productId);
        return ResponseEntity.ok(Map.of(
                "requested", existing.isPresent(),
                "notified", existing.map(a -> Boolean.TRUE.equals(a.getNotified())).orElse(false)
        ));
    }

    /** 신청 — 품절 상품만, 중복 방지 */
    @PostMapping
    @Transactional
    public ResponseEntity<?> request(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        Long productId = Long.parseLong(body.get("productId"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        if (stock > 0) {
            throw new IllegalArgumentException("품절 상품에만 재입고 알림을 신청할 수 있습니다.");
        }
        var existing = alertRepository.findByMemberIdAndProductId(member.getId(), productId);
        if (existing.isPresent()) {
            // 재입고 알림을 이미 받았던 신청이면 다시 대기 상태로 초기화
            RestockAlert alert = existing.get();
            if (Boolean.TRUE.equals(alert.getNotified())) {
                alert.setNotified(false);
                alert.setNotifiedAt(null);
                return ResponseEntity.ok(Map.of("message", "재입고 알림을 다시 신청했습니다."));
            }
            throw new IllegalArgumentException("이미 재입고 알림을 신청한 상품입니다.");
        }
        alertRepository.save(RestockAlert.builder().member(member).product(product).build());
        return ResponseEntity.ok(Map.of("message", "재입고 알림을 신청했습니다. 재입고 시 알려드립니다."));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        RestockAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));
        if (!alert.getMember().getUsername().equals(auth.getName())) {
            throw new IllegalArgumentException("본인의 신청만 취소할 수 있습니다.");
        }
        alertRepository.delete(alert);
        return ResponseEntity.ok(Map.of("message", "재입고 알림 신청을 취소했습니다."));
    }
}
