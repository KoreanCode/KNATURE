package com.knature.common.domain.product;

public enum ProductStatus {
    ON_SALE("판매중"),
    SOLD_OUT("품절"),
    HIDDEN("숨김");

    private final String label;

    ProductStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
