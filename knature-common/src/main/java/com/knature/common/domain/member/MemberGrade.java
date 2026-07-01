package com.knature.common.domain.member;

public enum MemberGrade {
    NEW("뉴", 0, 0),
    RUBY("루비", 50_000, 1),
    SILVER("실버", 500_000, 2),
    GOLD("골드", 1_500_000, 3),
    DIAMOND("다이아몬드", 3_000_000, 4),
    PLATINUM("플래티넘", 5_000_000, 5);

    private final String label;
    private final long minPurchase;
    private final int rewardRate;

    MemberGrade(String label, long minPurchase, int rewardRate) {
        this.label = label;
        this.minPurchase = minPurchase;
        this.rewardRate = rewardRate;
    }

    public String getLabel() { return label; }
    public long getMinPurchase() { return minPurchase; }
    public int getRewardRate() { return rewardRate; }
}
