package com.knature.common.repository;

import com.knature.common.domain.scm.FactoryStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactoryStockRepository extends JpaRepository<FactoryStock, Long> {
    List<FactoryStock> findByFactoryId(Long factoryId);

    Optional<FactoryStock> findByFactoryIdAndProductId(Long factoryId, Long productId);
}
