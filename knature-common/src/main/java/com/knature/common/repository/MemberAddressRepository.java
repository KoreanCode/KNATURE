package com.knature.common.repository;

import com.knature.common.domain.member.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    List<MemberAddress> findByMemberIdOrderByIsDefaultDescIdDesc(Long memberId);

    Optional<MemberAddress> findByIdAndMemberId(Long id, Long memberId);

    List<MemberAddress> findByMemberIdAndIsDefaultTrue(Long memberId);

    long countByMemberId(Long memberId);
}
