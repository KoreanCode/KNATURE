package com.knature.common.repository;

import com.knature.common.domain.display.ProductDisplay;
import com.knature.common.domain.display.ProductDisplay.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDisplayRepository extends JpaRepository<ProductDisplay, Long> {
    List<ProductDisplay> findBySectionOrderBySortOrderAscIdAsc(Section section);
    List<ProductDisplay> findAllByOrderBySectionAscSortOrderAscIdAsc();
    boolean existsBySectionAndProductId(Section section, Long productId);
}
