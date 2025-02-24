package com.dosgo.ddns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DDNSWorker extends Worker {
    public DDNSWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        DDNSService.checkAndUpdate(getApplicationContext());
        return Result.success();
    }
}