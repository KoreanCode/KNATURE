package com.knature.common.repository;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.MemberGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    Page<Member> findByNameContainingOrUsernameContaining(String name, String username, Pageable pageable);

    Page<Member> findByGrade(MemberGrade grade, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.active = true")
    long countActive();

    @Query("SELECT COUNT(m) FROM Member m WHERE FUNCTION('DATE', m.createdAt) = CURRENT_DATE")
    long countTodayJoined();
}
