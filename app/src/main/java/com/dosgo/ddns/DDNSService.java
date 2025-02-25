package com.dosgo.ddns;// DDNSService.java
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DDNSService extends Service {

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "ddns_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化操作
        startWorkManagerTask();

        createNotificationChannel();
        startForegroundWithNotification();
    }


    private void startForegroundWithNotification() {
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ddns service")
                .setContentText("ddns service runing...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ddns service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("ddns service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动 WorkManager 任务

        
        // 保持服务运行（根据需求选择不同返回值）
        return START_STICKY;
    }

    private void startWorkManagerTask() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DDNSWorker.class,
                15, // 默认间隔 15 分钟
                TimeUnit.MINUTES
        ).build();
        WorkManager workManager = WorkManager.getInstance(this);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 本例不需要绑定服务
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理资源
    }
}