package com.congxiaoyao.xber_admin.monitoring.model;

import com.congxiaoyao.xber_admin.utils.MathUtils;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class NormalTraceCtrl extends TraceCtrl{

    @Override
    public NormalTraceCtrl init(float refRotate, GpsSampleRsp start, GpsSampleRsp end) {
        super.init(refRotate, start, end);
        return this;
    }

    @Override
    protected void animate(float progress) {
        crtLat = MathUtils.map(0, 1, start.getLat(), end.getLat(), progress);
        crtLng = MathUtils.map(0, 1, start.getLng(), end.getLng(), progress);
        //在最后的两秒内将车头调转过来 但是如果整个运行时间不足两秒 则认为这是一个转向动画
        if (totalTime <= 2000) {
            rotate = MathUtils.map(0, 1, startRotate, endRotate, progress);
        } else if (elapsedTime < 1000) {
            rotate = MathUtils.map(0, 1000, startRotate, endRotate, elapsedTime);
        }
    }
}
