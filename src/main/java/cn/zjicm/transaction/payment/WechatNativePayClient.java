package cn.zjicm.transaction.payment;

import cn.zjicm.transaction.config.WechatPayProperties;
import cn.zjicm.transaction.model.PaymentOrder;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WechatNativePayClient {

    private final WechatPayProperties properties;
    private Config config;
    private NativePayService nativePayService;
    private NotificationParser notificationParser;

    public WechatNativePayClient(WechatPayProperties properties) {
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String createNativePayment(PaymentOrder order) {
        ensureConfigured();
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(order.getAmountInFen());
        request.setAmount(amount);
        request.setAppid(properties.getAppId());
        request.setMchid(properties.getMchId());
        request.setDescription(order.getTargetTitle());
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setOutTradeNo(order.getOrderNo());

        PrepayResponse response = nativePayService().prepay(request);
        return response.getCodeUrl();
    }

    public WechatPayNotifyResult parseNotify(Map<String, String> headers, String body) {
        ensureConfigured();
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(header(headers, "Wechatpay-Serial"))
                .nonce(header(headers, "Wechatpay-Nonce"))
                .signature(header(headers, "Wechatpay-Signature"))
                .timestamp(header(headers, "Wechatpay-Timestamp"))
                .body(body)
                .build();
        Transaction transaction = notificationParser().parse(requestParam, Transaction.class);
        return new WechatPayNotifyResult(
                transaction.getOutTradeNo(),
                Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())
        );
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new PaymentConfigurationException("微信支付未配置，请先设置商户号、APIv3 密钥、商户证书序列号、私钥路径和回调地址。");
        }
    }

    private NativePayService nativePayService() {
        if (nativePayService == null) {
            nativePayService = new NativePayService.Builder().config(config()).build();
        }
        return nativePayService;
    }

    private NotificationParser notificationParser() {
        if (notificationParser == null) {
            notificationParser = new NotificationParser((NotificationConfig) config());
        }
        return notificationParser;
    }

    private Config config() {
        if (config == null) {
            config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(properties.getMchId())
                    .privateKeyFromPath(properties.getPrivateKeyPath())
                    .merchantSerialNumber(properties.getMerchantSerialNumber())
                    .apiV3Key(properties.getApiV3Key())
                    .build();
        }
        return config;
    }

    private String header(Map<String, String> headers, String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("");
    }
}
