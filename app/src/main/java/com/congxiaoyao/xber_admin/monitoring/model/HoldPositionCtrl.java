package com.congxiaoyao.xber_admin.monitoring.model;

import com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/4/24.
 */

public class HoldPositionCtrl extends TraceCtrl {

    private GpsSampleRsp position;

    public HoldPositionCtrl init(float refRotate, GpsSampleRsp position, long time) {
        checkUsing(false, getClass().getSimpleName() + "已经被初始化 请回收后使用");
        this.position = position;
        start = position;
        end = position;
        totalTime = time;
        elapsedTime = 0;
        lastTime = 0;
        progress = 0;
        crtLat = position.getLat();
        crtLng = position.getLng();
        rotate = refRotate;
        isUsing = true;
        return this;
    }

    @Deprecated
    @Override
    public HoldPositionCtrl init(float refRotate, GpsSampleRsp start, GpsSampleRsp end) {
        super.init(refRotate, start, end);
        return this;
    }

    @Override
    protected void animate(float progress) {
    }

    public GpsSampleRsp getPosition() {
        return position;
    }
}
