package com.knature.common.repository;

import com.knature.common.domain.scm.StockLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLotRepository extends JpaRepository<StockLot, Long> {
    List<StockLot> findByProductIdOrderByExpiryDateAsc(Long productId);

    List<StockLot> findAllByOrderByExpiryDateAsc();

    List<StockLot> findByProductIdAndQuantityGreaterThanOrderByManufactureDateAscIdAsc(Long productId, int quantity);
}
