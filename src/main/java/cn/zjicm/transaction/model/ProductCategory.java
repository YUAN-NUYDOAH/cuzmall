package cn.zjicm.transaction.model;

public enum ProductCategory {
    DIGITAL("数码设备"),
    BOOK("教材书籍"),
    DAILY("生活用品"),
    CLOTHES("服饰鞋包"),
    SPORTS("运动户外"),
    OTHER("其他");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
