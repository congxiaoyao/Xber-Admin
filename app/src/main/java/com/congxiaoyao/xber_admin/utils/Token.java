package com.congxiaoyao.xber_admin.utils;

import android.os.Build;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public class Token {
    public static String value = "";

    public static void processTokenAndSave(String rawToken) {
        char[] chars = new char[10];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ((int) (Math.random() * 10) + 'a');
        }
        String header = new String(chars);
        //TODO delete this in real version
        value = String.format("Basic %s:%s", header + Build.SERIAL, rawToken);
    }
}
