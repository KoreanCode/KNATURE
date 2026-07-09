package com.knature.common.repository;

import com.knature.common.domain.mileage.MileageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {
    List<MileageHistory> findByMemberIdOrderByIdDesc(Long memberId);
}
