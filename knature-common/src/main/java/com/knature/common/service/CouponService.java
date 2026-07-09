package com.knature.common.service;

import com.knature.common.domain.coupon.Coupon;
import com.knature.common.domain.coupon.MemberCoupon;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.MemberGrade;
import com.knature.common.repository.CouponRepository;
import com.knature.common.repository.MemberCouponRepository;
import com.knature.common.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 쿠폰 공통 서비스 — BO(생성/발급)·FO(보유/사용) 공용 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;

    public List<Coupon> getCoupons() {
        return couponRepository.findAll();
    }

    public List<MemberCoupon> getMemberCoupons(Long memberId) {
        return memberCouponRepository.findByMemberIdOrderByIdDesc(memberId);
    }

    @Transactional
    public Coupon create(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    /** 발급 — target: MEMBER(memberId) / GRADE(grade) / ALL. 중복 발급은 건너뜀. 발급 건수 반환 */
    @Transactional
    public int issue(Long couponId, String target, Long memberId, MemberGrade grade) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        List<Member> targets = switch (target) {
            case "MEMBER" -> List.of(memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다.")));
            case "GRADE" -> memberRepository.findAll().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getActive()) && m.getGrade() == grade).toList();
            case "ALL" -> memberRepository.findAll().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getActive())).toList();
            default -> throw new IllegalArgumentException("잘못된 발급 대상입니다.");
        };
        int issued = 0;
        for (Member m : targets) {
            if (memberCouponRepository.existsByMemberIdAndCouponId(m.getId(), couponId)) continue;
            memberCouponRepository.save(MemberCoupon.builder().member(m).coupon(coupon).build());
            issued++;
        }
        return issued;
    }

    /** 코드 쿠폰 자동 발급 (예: 가입 WELCOME) — 쿠폰 미존재/중복 시 조용히 무시 */
    @Transactional
    public void issueByCode(Member member, String code) {
        couponRepository.findByCode(code).ifPresent(coupon -> {
            if (!Boolean.TRUE.equals(coupon.getActive())) return;
            if (memberCouponRepository.existsByMemberIdAndCouponId(member.getId(), coupon.getId())) return;
            memberCouponRepository.save(MemberCoupon.builder().member(member).coupon(coupon).build());
            log.info("자동 쿠폰 발급: {} → {}", code, member.getUsername());
        });
    }

    /** 사용 가능 검증 + 할인액 계산 (사용 처리는 호출자가 mc.use()) */
    public long validateAndCalculate(MemberCoupon mc, long orderAmount) {
        Coupon coupon = mc.getCoupon();
        if (Boolean.TRUE.equals(mc.getUsed())) {
            throw new IllegalArgumentException("이미 사용한 쿠폰입니다.");
        }
        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("유효기간이 지난 쿠폰입니다.");
        }
        if (orderAmount < coupon.getMinOrderAmount()) {
            throw new IllegalArgumentException("최소 주문금액 " + coupon.getMinOrderAmount() + "원 이상부터 사용 가능한 쿠폰입니다.");
        }
        return coupon.calculateDiscount(orderAmount);
    }
}
