package com.congxiaoyao.xber_admin;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.congxiaoyao.xber_admin.utils.Token;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class XberApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
        Token.value = "";
    }
}
