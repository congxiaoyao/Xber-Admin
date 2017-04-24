package com.congxiaoyao.xber_admin;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;

import static com.congxiaoyao.xber_admin.PushService.EXTRA_TOKEN;
import static com.congxiaoyao.xber_admin.PushService.EXTRA_URL;
import static com.congxiaoyao.xber_admin.PushService.EXTRA_USER_ID;

/**
 * Created by congxiaoyao on 2017/4/7.
 */

public class PushHelper extends JobService {

    private String token = "";
    private String url = "";
    private Long userId = -1L;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG.ME, "push helper handleMessage: ");
            PushService.startPushService(PushHelper.this, url, token, userId);
            return true;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG.ME, "push helper onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG.ME, "onStartJob: ");
        PersistableBundle extras = params.getExtras();
        token = extras.getString(EXTRA_TOKEN);
        url = extras.getString(EXTRA_URL);
        userId = extras.getLong(EXTRA_USER_ID, -1);
        handler.sendEmptyMessage(0);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        handler.removeCallbacksAndMessages(null);
        return false;
    }
}