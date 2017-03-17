package com.congxiaoyao.xber_admin.utils;

import android.content.Context;

import static android.provider.Settings.Secure;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public class Token {
    public static String value = "";

    public static void processTokenAndSave(Context context, String rawToken) {
        value = String.format("Basic %s:%s", getClientId(context), rawToken);
    }

    public static String getClientId(Context context) {
        return Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
    }
}
