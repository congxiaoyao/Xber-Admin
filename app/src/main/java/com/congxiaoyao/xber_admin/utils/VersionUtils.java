package com.congxiaoyao.xber_admin.utils;

import android.os.Build;

/**
 * Created by congxiaoyao on 2016/7/20.
 */
public class VersionUtils {

    public static final int ICE_CREAM = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1; //15
    public static final int JELLY_BEAN = Build.VERSION_CODES.JELLY_BEAN;            //16
    public static final int ANDROID_4_2 = Build.VERSION_CODES.JELLY_BEAN_MR1;
    public static final int ANDROID_4_3 = Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final int KITKAT = Build.VERSION_CODES.KITKAT;
    public static final int KITKAT_WATCH = Build.VERSION_CODES.KITKAT_WATCH;
    public static final int LOLLIPOP = Build.VERSION_CODES.LOLLIPOP;
    public static final int M = Build.VERSION_CODES.M;

    public static final int API_15 = 15;
    public static final int API_16 = 16;
    public static final int API_17 = 17;
    public static final int API_18 = 18;
    public static final int API_19 = 19;
    public static final int API_20 = 20;
    public static final int API_21 = 21;
    public static final int API_22 = 22;
    public static final int API_23 = 23;

    /**
     * 4.4及以上
     * @return
     */
    public static boolean KitKatAndPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;


    /**
     * 5.0及以上
     *
     * @return
     */
    public static boolean LollipopAndPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;


    /**
     * 那个版本及以上
     * @param version
     * @return
     */
    public static boolean isAndPlus(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

}
