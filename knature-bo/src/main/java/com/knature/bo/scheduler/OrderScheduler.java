package com.knature.bo.scheduler;

import com.knature.bo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 주문 관련 배치 — 무통장입금 7일 미입금 자동취소 (FO 무통장 안내와 동기화) */
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;

    /** 매시 정각 실행 */
    @Scheduled(cron = "0 0 * * * *")
    public void cancelExpiredBankTransferOrders() {
        orderService.cancelExpiredBankTransferOrders();
    }

    /** 기동 시 1회 실행 — 서버 중단 동안 밀린 자동취소 즉시 처리 */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        orderService.cancelExpiredBankTransferOrders();
    }
}
