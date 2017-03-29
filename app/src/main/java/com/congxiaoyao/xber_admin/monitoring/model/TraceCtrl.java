package com.congxiaoyao.xber_admin.monitoring.model;

import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public abstract class TraceCtrl {

    protected GpsSampleRsp ref;
    protected GpsSampleRsp start;
    protected GpsSampleRsp end;

    public double crtLat;
    public double crtLng;
    public float rotate;
    protected float progress = 0;

    protected long totalTime = 0;
    protected long elapsedTime = 0;
    protected long lastTime = 0;

    protected float endRotate;
    protected float startRotate;

    TraceCtrlFactory.RecycleBin recycleBin;

    public TraceCtrl init(GpsSampleRsp last, GpsSampleRsp start, GpsSampleRsp end) {
        this.start = start;
        this.end = end;
        totalTime = end.getTime() - start.getTime();
        if(totalTime <= 0) {
            totalTime = 0;
            progress = 1;
            return this;
        }
        startRotate = MathUtils.latLngToAngle(start.getLat() - last.getLat(),
                start.getLng() - last.getLng());
        endRotate = MathUtils.latLngToAngle(end.getLat() - start.getLat(),
                end.getLng() - start.getLng());
        return this;
    }

    public TraceCtrl init(GpsSampleRsp start, GpsSampleRsp end) {
        init(start, start, end);
        return this;
    }

    public LatLng getCurrentLatLng() {
        return new LatLng(crtLat, crtLng);
    }

    protected float getInterpolation(float progress) {
        return progress;
    }

    public boolean shouldStop(){
        return progress >= 1.0f;
    }

    public void handleAndPost(Handler handler) {
        if (progress > 1) progress = 1;
        if (progress < 0) progress = 0;
        if (lastTime == 0) lastTime = System.currentTimeMillis();

        //当前时间
        long now = System.currentTimeMillis();

        //计算此动画已经运行的时间
        elapsedTime += (now - lastTime);
		if (elapsedTime > totalTime) elapsedTime = totalTime;
        //计算此动画的当前进度
        progress = MathUtils.map(0, totalTime, 0, 1, elapsedTime);
        progress = getInterpolation(progress);

        //进行相应属性(经纬度)的计算
        animate(progress);

        Message message = Message.obtain();
        message.what = (int) start.getCarId();
        message.obj = this;

        //延时16毫秒计算下一帧，但依照具体时间而定 如果性能跟不上就不延时或少延时
        int animateTime = (int) (16 - (now - lastTime));
        if (animateTime > 0) {
            handler.sendMessageDelayed(message, animateTime);
        }else {
            handler.sendMessage(message);
        }

		lastTime = now;
    }

    protected abstract void animate(float progress);

    public void recycle() {
        recycleBin.recycle(this);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public GpsSampleRsp getStart() {
        return start;
    }

    public GpsSampleRsp getEnd() {
        return end;
    }

    public void setRef(GpsSampleRsp ref) {
        this.ref = ref;
    }
}
