package com.dosgo.ddns;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class DNSPodClient {
    private static final String API_URL = "https://dnsapi.cn/Record.Modify";
    private static final OkHttpClient client = new OkHttpClient();

    // 获取 Record ID
    public static String getRecordId(Config config,String recordType) throws IOException {
        String url = "https://dnsapi.cn/Record.List";
        FormBody body = new FormBody.Builder()
                .add("login_token", config.getApiId() + "," + config.getApiToken())
                .add("format", "json")
                .add("domain", config.getDomain())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            String json = response.body().string();

            JSONObject root = new JSONObject(json);
            JSONArray records = root.getJSONArray("records");
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                String name = record.getString("name");
                String type = record.getString("type");
                if (name.equals(config.getSubDomain()) && type.equals(recordType)) {
                    return record.getString("id");
                }
            }
            return null;
        } catch (JSONException e) {
            throw new IOException("Failed to parse response", e);
        }
    }

    public static String updateRecord(Config config, String newIP,String recordType) throws IOException {


            String recordId = getRecordId(config,recordType);
            if (recordId == null) {

                return "No record id";
            }
          //  String requestBody1 = new FormBody.Builder().add("ddd","ddd").build().toString()

            RequestBody requestBody = new FormBody.Builder()
                    .add("login_token", config.getApiId() + "," + config.getApiKey())
                    .add("format", "json")
                    .add("domain", config.getDomain())
                    .add("record_id", recordId)
                    .add("sub_domain", config.getSubDomain())
                    .add("record_type", recordType)
                    .add("record_line", "默认")
                    .add("value", newIP)
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            return response.body().string();

    }
}