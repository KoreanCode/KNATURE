package com.knature.bo.controller;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.mileage.MileageHistory.MileageType;
import com.knature.common.repository.MemberRepository;
import com.knature.common.service.MileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** BO 적립금 관리 — 회원별 조회 / 수동 지급·차감 (사유 필수) */
@RestController
@RequestMapping("/api/mileages")
@RequiredArgsConstructor
public class MileageAdminController {

    private final MileageService mileageService;
    private final MemberRepository memberRepository;

    @GetMapping("/{memberId}")
    public ResponseEntity<?> history(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return ResponseEntity.ok(Map.of(
                "memberId", member.getId(),
                "name", member.getName(),
                "username", member.getUsername(),
                "balance", member.getMileage() == null ? 0 : member.getMileage(),
                "history", mileageService.getHistory(memberId)
        ));
    }

    /** amount: +지급 / -차감, reason 필수 */
    @PostMapping("/{memberId}")
    @Transactional
    public ResponseEntity<?> adjust(@PathVariable Long memberId, @RequestBody Map<String, String> body) {
        long amount = Long.parseLong(body.get("amount"));
        String reason = body.get("reason");
        if (amount == 0) throw new IllegalArgumentException("금액을 입력해주세요.");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("사유를 입력해주세요.");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        mileageService.change(member, amount, MileageType.ADMIN, "[관리자] " + reason);
        return ResponseEntity.ok(Map.of("message", (amount > 0 ? "지급" : "차감") + " 완료 (잔액 " + member.getMileage() + "P)"));
    }
}
