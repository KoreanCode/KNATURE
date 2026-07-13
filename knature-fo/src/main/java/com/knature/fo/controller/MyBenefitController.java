package com.knature.fo.controller;

import com.knature.common.domain.member.Member;
import com.knature.common.repository.MemberRepository;
import com.knature.common.service.CouponService;
import com.knature.common.service.DepositService;
import com.knature.common.service.MileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** FO 마이페이지 — 적립금/쿠폰 내역 */
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyBenefitController {

    private final MemberRepository memberRepository;
    private final MileageService mileageService;
    private final CouponService couponService;
    private final DepositService depositService;

    private Member me(Authentication auth) {
        return memberRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @GetMapping("/mileage")
    public ResponseEntity<?> mileage(Authentication auth) {
        Member member = me(auth);
        return ResponseEntity.ok(Map.of(
                "balance", member.getMileage() == null ? 0 : member.getMileage(),
                "pending", mileageService.getPendingSum(member.getId()),
                "history", mileageService.getHistory(member.getId())
        ));
    }

    @GetMapping("/coupons")
    public ResponseEntity<?> coupons(Authentication auth) {
        return ResponseEntity.ok(couponService.getMemberCoupons(me(auth).getId()));
    }

    /** 예치금 내역 (2차) */
    @GetMapping("/deposit")
    public ResponseEntity<?> deposit(Authentication auth) {
        Member member = me(auth);
        return ResponseEntity.ok(Map.of(
                "balance", member.getDeposit() == null ? 0 : member.getDeposit(),
                "history", depositService.getHistory(member.getId())
        ));
    }
}
