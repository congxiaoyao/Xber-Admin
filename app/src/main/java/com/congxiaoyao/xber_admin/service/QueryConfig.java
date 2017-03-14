package com.congxiaoyao.xber_admin.service;

import java.util.concurrent.TimeUnit;

/**
 * Created by congxiaoyao on 2017/3/11.
 */

public class QueryConfig {

    /**
     * 时间单位 毫秒
     */
    public static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    /**
     * 普通轮询间隔时间
     */
    public static final int QUERY_DELAY_NORMAL = 5000;

    /**
     * 过弯时的轮询间隔时间
     */
    public static final int QUERY_DELAY_URGENT = 1000;

    /**
     * 当速度角度大于 TURNING_ANGLE 的时候则视为正在过弯
     */
    public static final double TURNING_ANGLE = Math.toRadians(15);

    /**
     * 数据点过期时间
     */
    public static final int DATA_EXPIRATION = 18000;

}
