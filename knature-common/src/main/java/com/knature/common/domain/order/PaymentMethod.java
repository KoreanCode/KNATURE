package com.knature.common.domain.order;

public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    BANK_TRANSFER("무통장입금"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이");

    private final String label;

    PaymentMethod(String label) { this.label = label; }
    public String getLabel() { return label; }
}
