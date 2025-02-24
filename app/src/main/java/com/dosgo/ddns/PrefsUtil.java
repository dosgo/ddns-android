package com.dosgo.ddns;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class PrefsUtil {
    private static final String PREFS_NAME = "ddns_config";
    private static final String KEY_CONFIG = "config";

    public static void saveConfig(Context context, Config config) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(config);
        prefs.edit().putString(KEY_CONFIG, json).apply();
    }

    public static Config loadConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CONFIG, "");
        return new Gson().fromJson(json, Config.class);
    }
}