package com.knature.common.repository;

import com.knature.common.domain.coupon.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    List<MemberCoupon> findByMemberIdOrderByIdDesc(Long memberId);

    Optional<MemberCoupon> findByIdAndMemberId(Long id, Long memberId);

    long countByCouponId(Long couponId);

    long countByCouponIdAndUsedTrue(Long couponId);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);
}
