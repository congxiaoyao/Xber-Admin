package com.congxiaoyao.xber_admin.utils;

import com.congxiaoyao.location.utils.Ray;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class MathUtils {

    private static final Ray ray = new Ray(0, 0, 0, 0);

    public static float map(float from1, float to1, float from2, float to2, float value) {
        float lenV = value - from1;
        float len1 = to1 - from1;
        float rate = lenV / len1;
        float len2 = to2 - from2;
        lenV = len2 * rate;
        return from2 + lenV;
    }

    public static double map(double from1, double to1, double from2, double to2, double value) {
        double lenV = value - from1;
        double len1 = to1 - from1;
        double rate = lenV / len1;
        double len2 = to2 - from2;
        lenV = len2 * rate;
        return from2 + lenV;
    }

    public static float latLngToAngle(double dLat, double dLng) {
        ray.setP1(dLng, dLat);
        return (float) Math.toDegrees(ray.getRayAngle());
    }

}
