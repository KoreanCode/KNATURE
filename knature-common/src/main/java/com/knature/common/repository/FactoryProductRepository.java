package com.knature.common.repository;

import com.knature.common.domain.scm.FactoryProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactoryProductRepository extends JpaRepository<FactoryProduct, Long> {
    List<FactoryProduct> findByFactoryId(Long factoryId);

    List<FactoryProduct> findByProductId(Long productId);

    Optional<FactoryProduct> findFirstByProductIdOrderByUnitCostAsc(Long productId);

    boolean existsByFactoryIdAndProductId(Long factoryId, Long productId);
}
