package com.knature.common.domain.order;

public enum OrderStatus {
    PENDING_PAYMENT("입금전"),
    PREPARING("배송준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송완료"),
    CANCEL_REQUESTED("취소요청"),
    CANCELLED("취소완료"),
    EXCHANGE_REQUESTED("교환요청"),
    EXCHANGE_COMPLETED("교환완료"),
    RETURN_REQUESTED("반품요청"),
    RETURN_COMPLETED("반품완료"),
    REFUND_COMPLETED("환불완료");

    private final String label;

    OrderStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
