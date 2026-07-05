package com.knature.common.repository;

import com.knature.common.domain.stock.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findByProductIdOrderByIdDesc(Long productId);
}
