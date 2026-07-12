package com.knature.common.repository;

import com.knature.common.domain.board.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByOrderByIdDesc();

    List<Inquiry> findByMemberIdOrderByIdDesc(Long memberId);

    Optional<Inquiry> findByIdAndMemberId(Long id, Long memberId);

    List<Inquiry> findByAnswerIsNullOrderByIdDesc();
}
