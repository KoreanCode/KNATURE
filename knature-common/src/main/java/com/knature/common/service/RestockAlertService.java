package com.knature.common.service;

import com.knature.common.domain.member.RestockAlert;
import com.knature.common.domain.product.Product;
import com.knature.common.repository.RestockAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 재입고 알림 (3차) — 재고 0 → 양수 전환 시 미처리 신청 건 자동 알림 처리.
 * 실제 발송 채널(메일/SMS) 연동 전까지는 처리 상태를 FO 알림 내역으로 노출한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestockAlertService {

    private final RestockAlertRepository alertRepository;

    /**
     * 재고 변동 후 호출 — 품절(0 이하)에서 판매 가능 수량으로 전환됐으면 대기 신청 건 알림 처리.
     * @param beforeQuantity 변동 전 재고
     */
    @Transactional
    public int notifyIfRestocked(Product product, int beforeQuantity) {
        int now = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        if (beforeQuantity > 0 || now <= 0) return 0;
        List<RestockAlert> waiting = alertRepository.findByProductIdAndNotifiedFalse(product.getId());
        for (RestockAlert alert : waiting) {
            alert.setNotified(true);
            alert.setNotifiedAt(LocalDateTime.now());
        }
        if (!waiting.isEmpty()) {
            log.info("재입고 알림 처리: {} — {}명 (모의 발송, FO 알림 내역 표시)", product.getName(), waiting.size());
        }
        return waiting.size();
    }
}
