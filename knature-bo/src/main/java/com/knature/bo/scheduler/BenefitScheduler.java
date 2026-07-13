package com.knature.bo.scheduler;

import com.knature.common.domain.coupon.Coupon;
import com.knature.common.domain.coupon.MemberCoupon;
import com.knature.common.repository.CouponRepository;
import com.knature.common.repository.MemberCouponRepository;
import com.knature.common.repository.MemberRepository;
import com.knature.common.service.MileageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 혜택 배치 (2차) — 생일 쿠폰 자동 발급 + 사용 대기 적립금 전환 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BenefitScheduler {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;
    private final MileageService mileageService;

    /** 매일 00:10 실행 */
    @Scheduled(cron = "0 10 0 * * *")
    public void daily() {
        runDaily();
    }

    /** 기동 시 1회 — 서버 중단 동안 밀린 배치 처리 */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        runDaily();
    }

    @Transactional
    public void runDaily() {
        int released = mileageService.releaseDuePending();
        int issued = issueBirthdayCoupons();
        if (released > 0 || issued > 0) {
            log.info("혜택 배치: 적립금 사용가능 전환 {}건, 생일 쿠폰 발급 {}건", released, issued);
        }
    }

    /** 오늘이 생일인 활성 회원에게 생일 쿠폰 발급 (올해 미발급자만) */
    private int issueBirthdayCoupons() {
        var birthdayCoupons = couponRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getBirthdayCoupon()) && Boolean.TRUE.equals(c.getActive()))
                .toList();
        if (birthdayCoupons.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDateTime yearStart = today.withDayOfYear(1).atStartOfDay();
        int issued = 0;
        for (var member : memberRepository.findAll()) {
            if (!Boolean.TRUE.equals(member.getActive()) || member.getBirthDate() == null) continue;
            if (member.getBirthDate().getMonthValue() != today.getMonthValue()
                    || member.getBirthDate().getDayOfMonth() != today.getDayOfMonth()) continue;
            for (Coupon coupon : birthdayCoupons) {
                if (memberCouponRepository.existsByMemberIdAndCouponIdAndCreatedAtAfter(
                        member.getId(), coupon.getId(), yearStart)) continue;
                memberCouponRepository.save(MemberCoupon.builder().member(member).coupon(coupon).build());
                issued++;
            }
        }
        return issued;
    }
}
