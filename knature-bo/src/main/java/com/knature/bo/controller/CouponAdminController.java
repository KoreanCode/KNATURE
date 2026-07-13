package com.knature.bo.controller;

import com.knature.common.domain.coupon.Coupon;
import com.knature.common.domain.member.MemberGrade;
import com.knature.common.repository.MemberCouponRepository;
import com.knature.common.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** BO 쿠폰 관리 — 생성 / 발급(회원·등급·전체) / 사용 현황 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponAdminController {

    private final CouponService couponService;
    private final MemberCouponRepository memberCouponRepository;

    @GetMapping
    public ResponseEntity<?> list() {
        List<Map<String, Object>> result = couponService.getCoupons().stream().map(c -> {
            long issued = memberCouponRepository.countByCouponId(c.getId());
            long used = memberCouponRepository.countByCouponIdAndUsedTrue(c.getId());
            return Map.<String, Object>of(
                    "coupon", c,
                    "issuedCount", issued,
                    "usedCount", used
            );
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        Coupon coupon = Coupon.builder()
                .code(blankToNull(body.get("code")))
                .name(required(body.get("name"), "쿠폰명"))
                .discountType(Coupon.DiscountType.valueOf(body.get("discountType")))
                .amount(Long.parseLong(required(body.get("amount"), "할인 금액/율")))
                .maxDiscount(body.get("maxDiscount") != null && !body.get("maxDiscount").isBlank()
                        ? Long.parseLong(body.get("maxDiscount")) : null)
                .minOrderAmount(body.get("minOrderAmount") != null && !body.get("minOrderAmount").isBlank()
                        ? Long.parseLong(body.get("minOrderAmount")) : 0L)
                .validUntil(body.get("validUntil") != null && !body.get("validUntil").isBlank()
                        ? LocalDate.parse(body.get("validUntil")) : null)
                .build();
        coupon.setBirthdayCoupon("true".equals(body.get("birthdayCoupon")));
        return ResponseEntity.ok(couponService.create(coupon));
    }

    /** 발급 — body: {target: MEMBER|GRADE|ALL, memberId?, grade?} */
    @PostMapping("/{id}/issue")
    public ResponseEntity<?> issue(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String target = required(body.get("target"), "발급 대상");
        Long memberId = body.get("memberId") != null && !body.get("memberId").isBlank()
                ? Long.parseLong(body.get("memberId")) : null;
        MemberGrade grade = body.get("grade") != null && !body.get("grade").isBlank()
                ? MemberGrade.valueOf(body.get("grade")) : null;
        int issued = couponService.issue(id, target, memberId, grade);
        return ResponseEntity.ok(Map.of("message", issued + "명에게 발급되었습니다."));
    }

    private String required(String v, String label) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(label + "을(를) 입력해주세요.");
        return v;
    }

    private String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v;
    }
}
