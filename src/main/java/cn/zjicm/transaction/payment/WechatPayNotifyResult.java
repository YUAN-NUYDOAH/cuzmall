package cn.zjicm.transaction.payment;

public record WechatPayNotifyResult(String orderNo, boolean paid) {
}
