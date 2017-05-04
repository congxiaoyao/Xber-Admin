package com.congxiaoyao.xber_admin.monitoring.model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.monitoring.RunningCar;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Xber车辆动画是基于关键点的补间动画，此类为补间动画轨迹控制类，用于计算一段轨迹中的每一时刻的位置
 * TraceCtrl是一个抽象类，他提供了一种基于自然时间流逝的轨迹计算方式
 * 在TraceCtrl中，存在总动画时间的概念 我们可以根据开始时间、流逝了的时间，计算出当前时间流逝的进度
 * 这个进度经过{@link TraceCtrl#getInterpolation(float)}做一步转换得到当前动画的进度
 * 有了此进度，子类可以此作为依据计算当前时间的位置。所以不同的子类可以做不同的实现来控制不同的效果
 * 最终计算结果将被保存在crtLat、crtLng、rotate中，代表着当前车辆的位置及旋转角度
 *
 * 同时 由于TraceCtrl工作在Handler中，他还负责下一帧消息的发送工作
 * 包括计算下一帧的时间并发送延时消息等
 *
 * 需要注意的是 TraceCtrl具有忽略补间点计算的能力，可以从任意时刻起，停止补间点的计算
 * 直到此动画结束。也就是说当暂停计算时，TraceCtrl会直接发送一个延时到动画结束的消息。
 * 除此之外，TraceCtrl也具有从任意时间恢复补间动画计算的能力，当然，为了使这个功能正常运行
 * 需要借助外部力量移除暂停时发送的那个延时消息
 *
 * 为了应对数据点超前、滞后、轻度转向、数据不足或正常运行等情况，我们创建了
 * {@link InfiniteTraceCtrl} {@link HoldPositionCtrl} {@link BazierTraceCtrl}
 * {@link NormalTraceCtrl} 等一系列子类，需要将其灵活运用方可达到令人满意的效果
 *
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
        LatLng latLng = new LatLng(crtLat, crtLng);
        return latLng;
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

    public void calculateAndPost(Handler handler, int currentAnimationState) {
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

        Message message = Message.obtain();
        message.what = (int) start.getCarId();
        message.obj = this;

        //进行相应属性(经纬度旋转角度)的计算
        animate(progress);

        //如果是静态轨迹(没有补间动画)则直接延时到动画结束
        if (currentAnimationState == RunningCar.STATE_STATIC) {
            handler.sendMessageDelayed(message, totalTime - elapsedTime);
            lastTime = now;
            return;
        }

        //在动态轨迹中 每一帧的时间间隔为16毫秒
        //但是在每一个TraceCtrl的最后一帧可能并不能延时完整的16
        long unitTime = totalTime - elapsedTime >= 16 ? 16 : totalTime - elapsedTime;

        //延时unitTime(理想情况16ms)计算下一帧 当然要把上面的那一小段代码执行的时间扣掉
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

    /**
     * 此函数的作用为将一个从暂停状态的TraceCtrl恢复为动画状态
     * 从开始暂停，到恢复动画，之间一定经历了一段时间，如果直接开始，会导致车辆位置闪现
     * 所以在恢复动画的过程中，唯一也是最重要的一件事就是忽略掉流逝的这段时间
     * 巧的是，我们可以利用lastTime为0的特性让他看起来像是刚刚被创建好一样
     * 但elapsedTime还保留了暂停时的初始值，使得TraceCtrl忽略了暂停状态到动画状态的时间
     * 从而可以完美的从暂停的位置中恢复出来
     */
    public void resumeFromStatic() {
        lastTime = 0;
    }

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
