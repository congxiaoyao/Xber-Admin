package com.congxiaoyao.xber_admin.monitoring.model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public abstract class TraceCtrl {

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

    public int id = 0;

    //标记是否正在被使用 为了保证计算之前init函数被调用以及正确的使用了回收池
    boolean isUsing = false;

    public TraceCtrl init(float refRotate, GpsSampleRsp start, GpsSampleRsp end) {
        checkUsing(false, getClass().getSimpleName() + "已经被初始化 请回收后使用");
        this.start = start;
        this.end = end;
        totalTime = end.getTime() - start.getTime();
        elapsedTime = 0;
        lastTime = 0;
        if (totalTime <= 0) {
            totalTime = 0;
            progress = 1;
            return this;
        }
        crtLat = start.getLat();
        crtLng = start.getLng();
        progress = 0;

        startRotate = refRotate;
        endRotate = MathUtils.latLngToAngle(end.getLat() - start.getLat(),
                end.getLng() - start.getLng());
        rotate = startRotate;
        isUsing = true;
        return this;
    }

    protected void checkUsing(boolean expect, String hint) {
        boolean b = (expect == isUsing);
        if (!b) {
            Log.d(TAG.ME, "checkUsing: expect is" + expect + " but here is " + isUsing);
            throw new RuntimeException(hint);
        }
    }

    public LatLng getCurrentLatLng() {
        return new LatLng(crtLat, crtLng);
    }

    public float getCurrentRotate() {
        return rotate;
    }

    protected float getInterpolation(float progress) {
        return progress;
    }

    public boolean shouldStop(){
        return progress >= 1.0f || !isUsing;
    }

    public void calculateAndPost(Handler handler) {
        checkUsing(true, "请先调用初始化函数进行初始化");

        if (lastTime == 0) lastTime = System.currentTimeMillis();

        //当前时间
        long now = System.currentTimeMillis();

        //计算此动画已经运行的时间
        elapsedTime += (now - lastTime);

        //先把lastTime置为now 为了把这段运算的时间也算进去 虽然基本上不会到1ms
        lastTime = now;

		if (elapsedTime > totalTime) elapsedTime = totalTime;
        //计算此动画的当前进度
        progress = MathUtils.map(0, totalTime, 0, 1, elapsedTime);
        progress = getInterpolation(progress);

        //进行相应属性(经纬度)的计算
        animate(progress);

        Message message = Message.obtain();
        message.what = (int) start.getCarId();
        message.obj = this;

        //每一帧的时间间隔为16毫秒
        //但是在每一个TraceCtrl的最后一帧可能并不能延时完整的16
        long unitTime = totalTime - elapsedTime >= 16 ? 16 : totalTime - elapsedTime;

        //延时unitTime(16ms)计算下一帧 当然要把上面的那一小段代码执行的时间扣掉
        long animateTime = unitTime - ((now = System.currentTimeMillis()) - lastTime);
        if (animateTime >= 0) {
            handler.sendMessageDelayed(message, animateTime);
        }else {
            Log.e(TAG.ME, "calculateAndPost: not delayed " + animateTime);
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
}
