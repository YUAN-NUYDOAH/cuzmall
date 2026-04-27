package cn.zjicm.transaction.model;

public enum ProductStatus {
    ON_SALE("在售"),
    RESERVED("已预订"),
    SOLD("已售出");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
