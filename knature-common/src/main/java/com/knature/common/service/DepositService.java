package com.knature.common.service;

import com.knature.common.domain.deposit.DepositHistory;
import com.knature.common.domain.deposit.DepositHistory.DepositType;
import com.knature.common.domain.member.Member;
import com.knature.common.repository.DepositHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 예치금 공통 서비스 — BO(지급/차감)·FO(조회/주문 사용/취소 환급) 공용 (2차) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepositService {

    private final DepositHistoryRepository historyRepository;

    public List<DepositHistory> getHistory(Long memberId) {
        return historyRepository.findByMemberIdOrderByIdDesc(memberId);
    }

    /**
     * 예치금 변동 (amount: +지급·환불 / -사용·차감). 잔액 부족 시 예외.
     * 호출자는 영속 상태의 Member 를 전달해야 한다 (dirty checking 저장).
     */
    @Transactional
    public DepositHistory change(Member member, long amount, DepositType type, String reason) {
        long balance = member.getDeposit() == null ? 0 : member.getDeposit();
        long after = balance + amount;
        if (after < 0) {
            throw new IllegalArgumentException("예치금이 부족합니다. (보유 " + balance + "원)");
        }
        member.setDeposit(after);
        return historyRepository.save(DepositHistory.builder()
                .member(member)
                .amount(amount)
                .balanceAfter(after)
                .depositType(type)
                .reason(reason)
                .build());
    }
}
