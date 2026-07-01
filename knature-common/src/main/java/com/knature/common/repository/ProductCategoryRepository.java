package com.knature.common.repository;

import com.knature.common.domain.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByParentIsNullOrderBySortOrder();

    List<ProductCategory> findByParentIdOrderBySortOrder(Long parentId);

    Optional<ProductCategory> findBySlug(String slug);
}
