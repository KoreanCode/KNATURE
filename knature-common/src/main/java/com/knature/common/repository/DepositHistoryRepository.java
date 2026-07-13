package com.knature.common.repository;

import com.knature.common.domain.deposit.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {
    List<DepositHistory> findByMemberIdOrderByIdDesc(Long memberId);
}
