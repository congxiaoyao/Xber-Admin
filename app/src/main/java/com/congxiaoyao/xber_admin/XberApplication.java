package com.congxiaoyao.xber_admin;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.congxiaoyao.xber_admin.settings.Settings;
import com.congxiaoyao.xber_admin.utils.Token;
import com.xiaomi.mipush.sdk.MiPushClient;

import net.orange_box.storebox.StoreBox;

import java.util.List;

import static android.app.ActivityManager.*;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class XberApplication extends Application {

    private Bitmap previewBitmap;
    public static final String APP_KEY = "5971757218356";
    public static final String APP_ID = "2882303761517572356";

    private Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();
        long pre = System.currentTimeMillis();
        if (settings == null) settings = StoreBox.create(this, Settings.class);
        if (shouldInit()) {
            SDKInitializer.initialize(this);
            if(settings.enableNotification())
                MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }
        Log.d("cxy", "time = " + (System.currentTimeMillis() - pre));
        Token.value = "";
    }

    public void cachePreviewBitmap(Bitmap bitmap) {
        this.previewBitmap = bitmap;
    }

    public void clearCachedBitmap() {
//        if (previewBitmap == null) return;
//        if (!previewBitmap.isRecycled()) {
//            previewBitmap.recycle();
//            previewBitmap = null;
//        }
        previewBitmap = null;
    }

    public Bitmap getCachedBitmap() {
        return previewBitmap;
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public Settings getSettings() {
        return settings;
    }
}
