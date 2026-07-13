package com.knature.common.repository;

import com.knature.common.domain.member.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByMemberIdOrderByIdDesc(Long memberId);
    Optional<Wishlist> findByMemberIdAndProductId(Long memberId, Long productId);
}
