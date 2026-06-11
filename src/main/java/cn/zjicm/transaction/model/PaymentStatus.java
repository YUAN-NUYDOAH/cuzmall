package cn.zjicm.transaction.model;

public enum PaymentStatus {
    PENDING("待支付"),
    PAID("已支付"),
    FAILED("支付失败");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
