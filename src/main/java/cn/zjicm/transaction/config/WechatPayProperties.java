package cn.zjicm.transaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

    private String appId;
    private String mchId;
    private String apiV3Key;
    private String merchantSerialNumber;
    private String privateKeyPath;
    private String notifyUrl;

    public boolean isConfigured() {
        return hasText(appId)
                && hasText(mchId)
                && hasText(apiV3Key)
                && hasText(merchantSerialNumber)
                && hasText(privateKeyPath)
                && hasText(notifyUrl);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getApiV3Key() {
        return apiV3Key;
    }

    public void setApiV3Key(String apiV3Key) {
        this.apiV3Key = apiV3Key;
    }

    public String getMerchantSerialNumber() {
        return merchantSerialNumber;
    }

    public void setMerchantSerialNumber(String merchantSerialNumber) {
        this.merchantSerialNumber = merchantSerialNumber;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
