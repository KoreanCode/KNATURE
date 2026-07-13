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

    /**
     * 대기 적립 (2차) — 잔액에 반영하지 않고 availableAt 도래 시 releaseDuePending() 이 전환.
     * (배송완료 N일 후 사용 가능 정책)
     */
    @Transactional
    public MileageHistory changePending(Member member, long amount, java.time.LocalDate availableAt,
                                        MileageType type, String reason) {
        MileageHistory history = MileageHistory.builder()
                .member(member)
                .amount(amount)
                .balanceAfter(member.getMileage() == null ? 0 : member.getMileage())
                .mileageType(type)
                .reason(reason)
                .build();
        history.setPending(true);
        history.setAvailableAt(availableAt);
        return historyRepository.save(history);
    }

    /** 사용 대기 적립 합계 */
    public long getPendingSum(Long memberId) {
        return historyRepository.findByMemberIdOrderByIdDesc(memberId).stream()
                .filter(h -> Boolean.TRUE.equals(h.getPending()))
                .mapToLong(MileageHistory::getAmount)
                .sum();
    }

    /** availableAt 도래한 대기 적립 → 잔액 반영 (스케줄러 일 배치) */
    @Transactional
    public int releaseDuePending() {
        var due = historyRepository.findByPendingTrueAndAvailableAtLessThanEqual(java.time.LocalDate.now());
        for (MileageHistory h : due) {
            Member member = h.getMember();
            long after = (member.getMileage() == null ? 0 : member.getMileage()) + h.getAmount();
            member.setMileage(after);
            h.setPending(false);
            h.setBalanceAfter(after);
        }
        return due.size();
    }

    /** 특정 주문의 대기 적립 무효화 (취소/반품 시) — reason 에 주문번호 포함 규약 */
    @Transactional
    public void voidPendingByOrder(Long memberId, String orderNumber) {
        historyRepository.findByMemberIdOrderByIdDesc(memberId).stream()
                .filter(h -> Boolean.TRUE.equals(h.getPending()) && h.getReason() != null
                        && h.getReason().contains(orderNumber))
                .forEach(historyRepository::delete);
    }
}
