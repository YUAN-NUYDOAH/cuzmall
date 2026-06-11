package cn.zjicm.transaction.model;

public enum PaymentTargetType {
    PRODUCT("二手商品"),
    SUBSTITUTE("代课");

    private final String displayName;

    PaymentTargetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
