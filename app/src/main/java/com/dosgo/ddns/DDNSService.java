package com.dosgo.ddns;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class DDNSService {
    private static final String TAG = "DDNSService";
    private static  String currentIP="";
    private static  String currentIPV6="";
    public static void checkAndUpdate(Context context) {
        Config config = PrefsUtil.loadConfig(context);
        if (config == null) return;
        LogUtils.appendLog(context, "checkAndUpdate");
        try {

            // 获取当前 IP
            if (config.isEnableIPv4()) {

                if(currentIP.isEmpty()){
                    String oldIP = IPUtils.resolveSingleAddress(config.getSubDomain() + "." + config.getDomain(), false);
                    if(!oldIP.isEmpty()){
                        currentIP=oldIP;
                    }
                }
                String newIP = IPUtils.getPublicIP(false);
                LogUtils.appendLog(context, "new IP:"+newIP);
                if(!newIP.equals(currentIP)) {
                    LogUtils.appendLog(context, "update new IP:" + newIP);
                    String res= updateDNS(config, newIP, "A");
                    LogUtils.appendLog(context, "update res:" + res);
                }
            }
            if (config.isEnableIPv6()) {

                if(currentIPV6.isEmpty()){
                    String oldIP = IPUtils.resolveSingleAddress(config.getSubDomain() + "." + config.getDomain(), true);
                    if(!oldIP.isEmpty()){
                        currentIPV6=oldIP;
                    }
                }

                String newIP = IPUtils.getPublicIP(true);
                LogUtils.appendLog(context, "new IPV6:" + newIP);
                if(!newIP.equals(currentIPV6)) {
                    LogUtils.appendLog(context, "update new IPV6:" + newIP);
                    String res=updateDNS(config, newIP, "AAAA");
                    LogUtils.appendLog(context, "update res:" + res);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "DDNS 更新失败: " + e.getMessage());
            e.printStackTrace();
            LogUtils.appendLog(context, "DDNS 更新失败:"+ e.getMessage());
        }
    }

    private static String updateDNS(Config config, String ip, String type) throws IOException {
        if ("cloudflare".equals(config.getProvider())) {
           return  CloudflareClient.updateRecord(config, ip);
        } else if ("dnspod".equals(config.getProvider())) {
            return  DNSPodClient.updateRecord(config, ip);
        }

        return "dns type config error";
    }
}