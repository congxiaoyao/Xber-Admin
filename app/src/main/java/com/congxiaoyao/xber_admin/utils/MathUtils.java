package com.congxiaoyao.xber_admin.utils;

import com.congxiaoyao.location.utils.Ray;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class MathUtils {

    public static float map(float from1, float to1, float from2, float to2, float value) {
        float lenV = value - from1;
        float len1 = to1 - from1;
        float rate = len1 == 0 ? 1 : lenV / len1;
        float len2 = to2 - from2;
        lenV = len2 * rate;
        return from2 + lenV;
    }

    public static double map(double from1, double to1, double from2, double to2, double value) {
        double lenV = value - from1;
        double len1 = to1 - from1;
        double rate = len1 == 0 ? 1 : lenV / len1;
        double len2 = to2 - from2;
        lenV = len2 * rate;
        return from2 + lenV;
    }

    public static float latLngToAngle(double dLat, double dLng) {
        return (float) Math.toDegrees(new Ray(0, 0,dLng, dLat).getRayAngle());
    }

    public static boolean amIFaster(double meLat, double meLng, double angle, double lat, double lng) {
        Ray ray = new Ray(0, 0, 1, 0);
        ray.rotate(Math.toRadians(angle));
        ray.rotate(-Math.PI / 2);
        ray.translate(meLng, meLat);
        int x = ray.whichSide(lng, lat);
        return x == 1;
    }
}
