package com.congxiaoyao.xber_admin.monitoring.model;

import android.util.Log;

import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.service.QueryConfig;
import com.congxiaoyao.xber_admin.utils.MathUtils;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class InfiniteTraceCtrl extends TraceCtrl {

    private double startLat,endLat,startLng, endLng;

    @Override
    public TraceCtrl init(float refRotate, GpsSampleRsp start, GpsSampleRsp end) {
        throw new RuntimeException("不能通过此方法初始化 InfiniteTraceCtrl");
    }

    public InfiniteTraceCtrl init(float refRotate, GpsSampleRsp point) {
        checkUsing(false, getClass().getSimpleName() + "已经被初始化 请回收后使用");
        progress = 0;
        elapsedTime = 0;
        lastTime = 0;

        //无限远动画情况下 可以直接认为动画时间为数据查询过期时间的两倍
        //因为在这段时间内 要么会有新数据产生 要么数据过期车辆就消失了
        totalTime = QueryConfig.DATA_EXPIRATION * 2;

        //只记录起始点就可以 为了兼容父类的处理方法
        start = point;

        //实际用于动画计算的是另一套变量
        startLat = crtLat = point.getLat();
        startLng = crtLng = point.getLng();
        endLat = startLat + start.getVlat() * (totalTime / 1000);
        endLng = startLng + start.getVlng() * (totalTime / 1000);

        //不做旋转动画 所以直接给rotate赋值
        rotate = startRotate = refRotate;
        endRotate = MathUtils.latLngToAngle(point.getVlat(), point.getVlng());
        isUsing = true;
        return this;
    }

    @Override
    protected void animate(float progress) {
        crtLat = MathUtils.map(0, 1, startLat, endLat, progress);
        crtLng = MathUtils.map(0, 1, startLng, endLng, progress);
        //花两百毫秒将车头调转 一般情况下是不需要调转车头的
        long refTime = totalTime < 200 ? totalTime : 200;
        if (elapsedTime <= refTime && rotate != endRotate) {
            rotate = MathUtils.map(0, refTime, startRotate, endRotate, elapsedTime);
        } else {
            rotate = endRotate;
        }
    }
}
