package com.knature.bo.service;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.MemberGrade;
import com.knature.common.domain.order.Order;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    public Page<Member> getMembers(String keyword, MemberGrade grade, Pageable pageable) {
        if (grade != null) {
            return memberRepository.findByGrade(grade, pageable);
        }
        if (keyword != null && !keyword.isBlank()) {
            return memberRepository.findByNameContainingOrUsernameContaining(keyword, keyword, pageable);
        }
        return memberRepository.findAll(pageable);
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + id));
    }

    public List<Order> getMemberOrders(Long id) {
        return orderRepository.findByMemberIdOrderByIdDesc(id);
    }

    /** 엑셀 다운로드용 전체 조회 (필터 동일 적용) */
    public List<Member> getMembersForExcel(String keyword, MemberGrade grade) {
        return getMembers(keyword, grade, Pageable.unpaged()).getContent();
    }

    @Transactional
    public void updateGrade(Long id, MemberGrade grade) {
        Member member = getMember(id);
        member.setGrade(grade);
    }
}
