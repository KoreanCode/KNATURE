package com.knature.fo.controller;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.Wishlist;
import com.knature.common.domain.product.Product;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.ProductRepository;
import com.knature.common.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** FO 위시리스트(찜) — 토글/목록 (2차) */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    private Member me(Authentication auth) {
        return memberRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        return ResponseEntity.ok(wishlistRepository.findByMemberIdOrderByIdDesc(me(auth).getId()));
    }

    /** 특정 상품 찜 여부 */
    @GetMapping("/check/{productId}")
    public ResponseEntity<?> check(@PathVariable Long productId, Authentication auth) {
        boolean wished = wishlistRepository.findByMemberIdAndProductId(me(auth).getId(), productId).isPresent();
        return ResponseEntity.ok(Map.of("wished", wished));
    }

    /** 토글 — 있으면 해제, 없으면 추가 */
    @PostMapping("/toggle")
    @Transactional
    public ResponseEntity<?> toggle(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        Long productId = Long.parseLong(body.get("productId"));
        var existing = wishlistRepository.findByMemberIdAndProductId(member.getId(), productId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return ResponseEntity.ok(Map.of("wished", false, "message", "위시리스트에서 제외했습니다."));
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        wishlistRepository.save(Wishlist.builder().member(member).product(product).build());
        return ResponseEntity.ok(Map.of("wished", true, "message", "위시리스트에 담았습니다."));
    }
}
