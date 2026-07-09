package com.knature.common.service;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.mileage.MileageHistory;
import com.knature.common.domain.mileage.MileageHistory.MileageType;
import com.knature.common.repository.MileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 적립금 공통 서비스 — BO(수동 지급/차감)·FO(가입/구매적립/사용) 공용 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageService {

    private final MileageHistoryRepository historyRepository;

    public List<MileageHistory> getHistory(Long memberId) {
        return historyRepository.findByMemberIdOrderByIdDesc(memberId);
    }

    /**
     * 적립금 변동 (amount: +적립 / -사용·차감). 잔액 부족 시 예외.
     * 호출자는 영속 상태의 Member 를 전달해야 한다 (dirty checking 저장).
     */
    @Transactional
    public MileageHistory change(Member member, long amount, MileageType type, String reason) {
        long balance = member.getMileage() == null ? 0 : member.getMileage();
        long after = balance + amount;
        if (after < 0) {
            throw new IllegalArgumentException("적립금이 부족합니다. (보유 " + balance + "P)");
        }
        member.setMileage(after);
        return historyRepository.save(MileageHistory.builder()
                .member(member)
                .amount(amount)
                .balanceAfter(after)
                .mileageType(type)
                .reason(reason)
                .build());
    }
}
