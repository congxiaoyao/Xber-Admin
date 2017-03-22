package com.congxiaoyao.xber_admin.utils;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class MathUtils {

    public static float map(float from1, float to1, float from2, float to2, float value) {
        float lenV = value - from1;
        float len1 = to1 - from1;
        float rate = lenV / len1;
        float len2 = to2 - from2;
        lenV = len2 * rate;
        return from2 + lenV;
    }
}
