package com.congxiaoyao.xber_admin.monitoring.model;

import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.xber_admin.utils.MathUtils;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class NormalTraceCtrl extends TraceCtrl{

    @Override
    protected void animate(float progress) {
        crtLat = MathUtils.map(0, 1, start.getLat(), end.getLat(), progress);
        crtLng = MathUtils.map(0, 1, start.getLng(), end.getLng(), progress);
        if (progress <= 0.2) {
            rotate = MathUtils.map(0, 0.2f, startRotate, endRotate, progress);
        }else {
            rotate = endRotate;
        }
    }

    @Override
    public NormalTraceCtrl init(GpsSampleRspOuterClass.GpsSampleRsp start, GpsSampleRspOuterClass.GpsSampleRsp end) {
        super.init(start, end);
        return this;
    }

    @Override
    public NormalTraceCtrl init(GpsSampleRspOuterClass.GpsSampleRsp last, GpsSampleRspOuterClass.GpsSampleRsp start, GpsSampleRspOuterClass.GpsSampleRsp end) {
        super.init(last, start, end);
        return this;
    }
}
