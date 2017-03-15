package com.congxiaoyao.xber_admin.utils;

import android.os.Build;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public class Token {
    public static String value = "";

    public static void processTokenAndSave(String rawToken) {
        value = String.format("Basic %s:%s", Build.SERIAL, rawToken);
    }
}
