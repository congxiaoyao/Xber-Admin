package com.congxiaoyao.xber_admin;

import android.app.Application;
import android.graphics.Bitmap;

import com.baidu.mapapi.SDKInitializer;
import com.congxiaoyao.xber_admin.utils.Token;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class XberApplication extends Application {

    private Bitmap previewBitmap;

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
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
}
