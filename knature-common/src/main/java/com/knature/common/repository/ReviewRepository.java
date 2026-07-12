package com.knature.common.repository;

import com.knature.common.domain.board.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByOrderByIdDesc();

    List<Review> findByProductIdAndBlindedFalseOrderByIdDesc(Long productId);

    List<Review> findByProductIdOrderByIdDesc(Long productId);

    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}
