package com.dosgo.ddns;

public class Config {
    private String provider;       // cloudflare/dnspod
    private String apiKey;         // Cloudflare API Key 或 DNSPod API Token

    private String apiId;          // DNSPod 专用 API ID
    private String domain;         // 要更新的域名（如 example.com）
    private String subDomain;      // 子域名（如 @ 或 www）
    private boolean enableIPv4;
    private boolean enableIPv6;


    private boolean ipv6Privacy;

    private String apiToken;       // DNSPod API Token

    // Cloudflare 专用字段
    private String zoneId;         // Cloudflare Zone ID



    public boolean isEnableIPv4() {
        return enableIPv4;
    }

    public void setEnableIPv4(boolean enableIPv4) {
        this.enableIPv4 = enableIPv4;
    }

    public boolean isEnableIPv6() {
        return enableIPv6;
    }

    public void setEnableIPv6(boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
    }



    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public boolean isIpv6Privacy() {
        return ipv6Privacy;
    }

    public void setIpv6Privacy(boolean ipv6Privacy) {
        this.ipv6Privacy = ipv6Privacy;
    }
}