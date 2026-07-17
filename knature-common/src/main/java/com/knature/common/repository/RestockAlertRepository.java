package com.knature.common.repository;

import com.knature.common.domain.member.RestockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestockAlertRepository extends JpaRepository<RestockAlert, Long> {
    List<RestockAlert> findByMemberIdOrderByIdDesc(Long memberId);
    Optional<RestockAlert> findByMemberIdAndProductId(Long memberId, Long productId);
    List<RestockAlert> findByProductIdAndNotifiedFalse(Long productId);
}
