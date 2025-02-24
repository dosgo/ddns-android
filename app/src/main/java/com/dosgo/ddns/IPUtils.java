package com.dosgo.ddns;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IPUtils {
    public static String getPublicIP(boolean isIPv6) {
        OkHttpClient client = new OkHttpClient();
        String url = isIPv6 ? "https://api64.ipify.org" : "https://ipinfo.io/ip";
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            return null;
        }
    }




    // 通用方法：根据 IP 类型过滤地址
    public static List<String> getLocalIP(Class<? extends InetAddress> ipType) {
        List<String> ipList = new ArrayList<>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                // 过滤未启用或回环接口
                if (!intf.isUp() || intf.isLoopback()) continue;


                // 遍历接口的 IP 地址
                List<InetAddress> addresses = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addresses) {
                    if (!addr.isLoopbackAddress() && ipType.isInstance(addr)&&!addr.isLinkLocalAddress()) {
                        String hostAddr = addr.getHostAddress();
                        // 过滤 IPv6 的 Zone ID（如 fe80::1%eth0）
                        int zoneIdIndex = hostAddr.indexOf('%');
                        if (zoneIdIndex > 0) {
                            hostAddr = hostAddr.substring(0, zoneIdIndex);
                        }
                        ipList.add(hostAddr);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipList;
    }

    // 示例：获取第一个可用的 IPv4 地址
    public static String getFirstIPv4() {
        List<String> ips = getLocalIP(Inet4Address.class);
        return ips.isEmpty() ? null : ips.get(0);
    }

    // 示例：获取第一个可用的 IPv6 地址
    public static String getFirstIPv6() {
        List<String> ips =  getLocalIP(Inet6Address.class);
        return ips.isEmpty() ? null : ips.get(0);
    }


    public static String resolveSingleAddress(String hostname, boolean isIPv6) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            for (InetAddress addr : addresses) {
                if (isIPv6 && addr instanceof Inet6Address) {
                    return addr.getHostAddress();
                } else if (!isIPv6 && addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
            return null; // 未找到指定类型的 IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return ""; // 解析失败
        }
    }

}