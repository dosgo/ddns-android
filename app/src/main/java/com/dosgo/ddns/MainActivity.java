package com.dosgo.ddns;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.*;
import com.google.gson.Gson;
import okhttp3.*;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText etApiKey, etZoneId, etApiId, etApiToken, etDomain, etSubDomain, etInterval;
    private CheckBox cbIPv4, cbIPv6;
    private Spinner spProvider;
    private Button btnToggle;
    private LinearLayout cloudflareConfig, dnspodConfig;
    private WorkManager workManager;
    private TextView tvLogs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initViews();

        // 加载已有配置
        loadConfig();

        // 提供商切换监听
        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateProviderUI(position == 0 ? "cloudflare" : "dnspod");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 保存配置
        findViewById(R.id.btnSave).setOnClickListener(v -> saveConfig());


        btnToggle = findViewById(R.id.btnToggle);
        workManager = WorkManager.getInstance(this);
        // 监听 WorkManager 状态
        workManager.getWorkInfosForUniqueWorkLiveData("DDNS_Service")
                .observe(this, workInfos -> {
                    boolean isRunning = false;
                    if (workInfos != null) {
                        for (WorkInfo info : workInfos) {
                            if (info.getState() == WorkInfo.State.ENQUEUED ||
                                    info.getState() == WorkInfo.State.RUNNING) {
                                isRunning = true;
                                break;
                            }
                        }
                    }
                    btnToggle.setText(isRunning ? "停止服务" : "启动服务");
                });

        // 按钮点击切换服务状态
        btnToggle.setOnClickListener(v -> {
            if (btnToggle.getText().toString().contains("启动")) {
                startService();
            } else {
                stopService();
            }
        });

        tvLogs = findViewById(R.id.tvLogs);
        workManager = WorkManager.getInstance(this);

        // 初始化日志显示
        refreshLogs();

        // 清空日志
        findViewById(R.id.btnClearLogs).setOnClickListener(v -> {
            LogUtils.clearLogs(MainActivity.this);
            tvLogs.setText("");
        });

        // 监听日志文件变化（每 2 秒刷新一次）
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> refreshLogs());
            }
        }, 0, 2000);
    }

    private void initViews() {
        spProvider = findViewById(R.id.spProvider);
        cloudflareConfig = findViewById(R.id.cloudflareConfig);
        dnspodConfig = findViewById(R.id.dnspodConfig);
        etApiKey = findViewById(R.id.etApiKey);
        etZoneId = findViewById(R.id.etZoneId);
        etApiId = findViewById(R.id.etApiId);
        etApiToken = findViewById(R.id.etApiToken);
        etDomain = findViewById(R.id.etDomain);
        etSubDomain = findViewById(R.id.etSubDomain);
        cbIPv4 = findViewById(R.id.cbIPv4);
        cbIPv6 = findViewById(R.id.cbIPv6);
        etInterval = findViewById(R.id.etInterval);
    }

    private void updateProviderUI(String provider) {
        cloudflareConfig.setVisibility(provider.equals("cloudflare") ? View.VISIBLE : View.GONE);
        dnspodConfig.setVisibility(provider.equals("dnspod") ? View.VISIBLE : View.GONE);
    }

    private void saveConfig() {
        Config config = new Config();
        config.setProvider(spProvider.getSelectedItem().toString().toLowerCase());
        config.setApiKey(etApiKey.getText().toString());
        config.setZoneId(etZoneId.getText().toString());
        config.setApiId(etApiId.getText().toString());
        config.setApiToken(etApiToken.getText().toString());
        config.setDomain(etDomain.getText().toString());
        config.setSubDomain(etSubDomain.getText().toString());
        config.setEnableIPv4(cbIPv4.isChecked());
        config.setEnableIPv6(cbIPv6.isChecked());
        if(!etInterval.getText().toString().equals("")) {
            config.setIntervalMinutes(Integer.parseInt(etInterval.getText().toString()));
        }

        // 保存到 SharedPreferences
        getSharedPreferences("ddns_config", MODE_PRIVATE)
                .edit()
                .putString("config", new Gson().toJson(config))
                .apply();

        Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show();
    }

    private void loadConfig() {
        String json = getSharedPreferences("ddns_config", MODE_PRIVATE)
                .getString("config", "");

        if (!json.isEmpty()) {
            Config config = new Gson().fromJson(json, Config.class);
            spProvider.setSelection(config.getProvider().equals("cloudflare") ? 0 : 1);
            etApiKey.setText(config.getApiKey());
            etZoneId.setText(config.getZoneId());
            etApiId.setText(config.getApiId());
            etApiToken.setText(config.getApiToken());
            etDomain.setText(config.getDomain());
            etSubDomain.setText(config.getSubDomain());
            cbIPv4.setChecked(config.isEnableIPv4());
            cbIPv6.setChecked(config.isEnableIPv6());
            etInterval.setText(String.valueOf(config.getIntervalMinutes()));
            updateProviderUI(config.getProvider());
        }
    }

    // 启动服务
    private void startService() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DDNSWorker.class,
                15, // 默认间隔 15 分钟
                TimeUnit.MINUTES
        ).build();

        workManager.enqueueUniquePeriodicWork(
                "DDNS_Service",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
        );
        LogUtils.appendLog(this, "startService");

        List<String> ips =IPUtils.getLocalIP(Inet4Address.class);
        for (String ip :ips) {
            LogUtils.appendLog(this, "ip:"+ip);
        }
        List<String> ipsv6 =IPUtils.getLocalIP(Inet6Address.class);
        for (String ip :ipsv6) {
            LogUtils.appendLog(this, "ipv6:"+ip);
        }

    }

    // 停止服务
    private void stopService() {
        LogUtils.appendLog(this, "stopService");
        workManager.cancelUniqueWork("DDNS_Service");
    }


    // 刷新日志显示
    private void refreshLogs() {
        String logs = LogUtils.readLogs(this);
        tvLogs.setText(logs);

        // 自动滚动到底部
        final int scrollAmount =tvLogs.getLineCount();
        if (scrollAmount > 0) {
            tvLogs.scrollTo(0, scrollAmount);
        } else {
            tvLogs.scrollTo(0, 0);
        }
    }
}