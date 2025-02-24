package com.dosgo.ddns;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class CloudflareClient {
    private static final String TAG = "CloudflareClient";
    private static final String BASE_URL = "https://api.cloudflare.com/client/v4/zones/";
    private static final OkHttpClient client = new OkHttpClient();
    // 根据域名和子域名获取 Record ID
    public static String getRecordId(Config config) throws IOException {
        String url = "https://api.cloudflare.com/client/v4/zones/" + config.getZoneId() + "/dns_records";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            String json = response.body().string();

            // 解析 JSON 查找匹配的记录
            JSONObject root = new JSONObject(json);
            JSONArray records = root.getJSONArray("result");
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                String name = record.getString("name");
                String type = record.getString("type");
                if (name.equals(config.getDomain()) && type.equals(config.getRecordType())) {
                    return record.getString("id");
                }
            }
            return null;
        } catch (JSONException e) {
            throw new IOException("解析响应失败", e);
        }
    }

    public static String updateRecord(Config config, String newIP) throws IOException {
        if (config == null || newIP == null || newIP.isEmpty()) {
            Log.e(TAG, "无效的配置或 IP 地址");
            return "无效的配置或 IP 地址";
        }

            String recordId = getRecordId(config);
            if (recordId == null) {
                Log.e(TAG, "未找到匹配的 DNS 记录");
                return "未找到匹配的 DNS 记录";
            }
            // 构建 API 请求 URL
            String url = BASE_URL + config.getZoneId() + "/dns_records/" + recordId;

            // 构建 JSON 请求体
            String jsonBody = String.format(
                    "{\"type\":\"%s\",\"name\":\"%s\",\"content\":\"%s\",\"ttl\":120}",
                    config.getRecordType(),
                    config.getSubDomain().isEmpty() ? config.getDomain() : config.getSubDomain() + "." + config.getDomain(),
                    newIP
            );

            // 创建 HTTP 请求
            Request request = new Request.Builder()
                    .url(url)
                    .put(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 执行请求
            Response response = new OkHttpClient().newCall(request).execute();

            return response.body().string();

    }
}