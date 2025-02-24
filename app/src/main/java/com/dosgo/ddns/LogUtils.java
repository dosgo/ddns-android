package com.dosgo.ddns;

import android.content.Context;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class LogUtils {
    private static final String LOG_FILE = "ddns.log";
    private static final int MAX_LOG_LINES = 200; // 最多保留 200 行日志

    // 添加日志（线程安全）
    public static synchronized void appendLog(Context context, String message) {
        String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String logLine = timestamp + " - " + message + "\n";

        // 写入文件
        try (FileOutputStream fos = context.openFileOutput(LOG_FILE, Context.MODE_APPEND)) {
            fos.write(logLine.getBytes());
            trimLogFile(context); // 清理旧日志
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 清理过期日志
    private static void trimLogFile(Context context) {
        try {
            File file = new File(context.getFilesDir(), LOG_FILE);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            long length = raf.length();
            if (length > 1024 * 1024) { // 文件超过 1MB 时清空
                raf.setLength(0);
                raf.close();
                return;
            }

            // 读取最后 MAX_LOG_LINES 行
            LinkedList<String> lines = new LinkedList<>();
            raf.seek(0);
            String line;
            while ((line = raf.readLine()) != null) {
                lines.add(line);
                if (lines.size() > MAX_LOG_LINES) {
                    lines.removeFirst();
                }
            }

            // 重写文件
            raf.setLength(0);
            for (String l : lines) {
                raf.writeBytes(l + "\n");
            }
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取日志
    public static String readLogs(Context context) {
        try (FileInputStream fis = context.openFileInput(LOG_FILE)) {
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            return "暂无日志";
        }
    }


    public static void clearLogs(Context context) {
        try (FileOutputStream fos = context.openFileOutput(LOG_FILE,context.MODE_PRIVATE)) {
            fos.write("".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}