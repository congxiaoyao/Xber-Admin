package com.congxiaoyao.xber_admin.monitoring;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.location.utils.Line;
import com.congxiaoyao.location.utils.VectorD;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.monitoring.model.HoldPositionCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.InfiniteTraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.NormalTraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import javax.inject.Inject;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class RunningCar extends Handler implements SyncOrderedList.DataReceiveListener{

    public static final String KEY_CAR_ID = "CAR_ID";
    public static final String tag = RunningCar.class.getSimpleName();
    public static final int WAIT_TIME = 1000;
    public static final int MAX_PURSUE_TIME = 5000;
    public static final int STATE_STATIC = 0;
    public static final int STATE_DYNAMIC = 1;
    public static final int ARG_RESUME = 100;

    private long carID;
    private final Marker marker;
    private SyncOrderedList<GpsSampleRsp> data;

    private TraceCtrl currentTrace;
    private int currentAnimationState;

    @Inject TextureMapView mapView;
    @Inject BaiduMap map;
    @Inject TraceCtrlFactory factory;

    private boolean destroy = false;

    private GpsSampleRsp.Builder builder = GpsSampleRsp.newBuilder();
    private LatLng currentLatLng;

    public RunningCar(XberMonitor xberMonitor, SyncOrderedList<GpsSampleRsp> data,
                      int currentAnimationState) {
        super();
        this.currentAnimationState = currentAnimationState;
        DaggerXberMonitor_MonitorComponent.builder().xberMonitor(xberMonitor)
                .build().inject(this);
        this.data = data;
        if (data == null || data.size() == 0) {
            currentLatLng = new LatLng(0, 0);
            throw new RuntimeException("上来给了我一个空的list?? 是不是出错了");
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_red_car));
        GpsSampleRsp gpsSampleRsp = data.takeFirst();
        markerOptions.position(currentLatLng = toLatLnt(gpsSampleRsp));
        markerOptions.anchor(0.5f, 0.5f);
        marker = (Marker) map.addOverlay(markerOptions);
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_CAR_ID, carID = gpsSampleRsp.getCarId());
        marker.setExtraInfo(bundle);

        //开始播放动画
        sendMessage(data.isEmpty() ? createHoldMessage(MathUtils
                .latLngToAngle(gpsSampleRsp.getVlat(),
                        gpsSampleRsp.getVlng()), gpsSampleRsp, WAIT_TIME) :
                createNormalMessage(MathUtils.latLngToAngle(gpsSampleRsp.getVlat(),
                        gpsSampleRsp.getVlng()), gpsSampleRsp, data.getFirst()));
        data.setCallback(this);
    }

    @Override
    public void handleMessage(Message msg) {
        if (destroy) return;

        //处理从静态恢复为动态的特殊消息
        if (msg.arg1 == ARG_RESUME) {
            removeMessages((int) carID);
            currentTrace.resumeFromStatic();
            sendMessage(createMessage(currentTrace));
            return;
        }

        TraceCtrl traceCtrl = (TraceCtrl) msg.obj;
        //此段轨迹动画播放结束
        if (traceCtrl.shouldStop()) {
            dispatchNewTraceCtrl(traceCtrl);
            return;
        }
        currentTrace = traceCtrl;
        //计算这一帧的位置 并发送延时消息准备计算下一帧
        traceCtrl.calculateAndPost(this, currentAnimationState);

        //根据计算结果更新位置
        currentLatLng = traceCtrl.getCurrentLatLng();
        marker.setPosition(currentLatLng);
        marker.setRotate(traceCtrl.getCurrentRotate());
    }

    private void dispatchNewTraceCtrl(TraceCtrl traceCtrl) {
        traceCtrl.recycle();
        //原地等待结束,但是依然数据量不足
        if (traceCtrl instanceof HoldPositionCtrl && data.size() < 2) {
            //那就再等一段时间
            Log.d(TAG.ME, "dispatchNewTraceCtrl: waiting");
            sendMessage(createHoldMessage(traceCtrl.rotate,
                    ((HoldPositionCtrl) traceCtrl).getPosition(), WAIT_TIME));
            return;
        }
        //从推测的轨迹中切换过来 所以不一定跟新的点能很好的契合 需要创建一段过渡轨迹
        if (traceCtrl instanceof InfiniteTraceCtrl || traceCtrl instanceof HoldPositionCtrl) {
            GpsSampleRsp position = data.getFirst();
            //这是一种异常情况 正常情况下基本不会出现
            if (position == null) {
                Log.e(TAG.ME, "dispatchNewTraceCtrl: InfiniteTraceCtrl结束但是还是没有数据");
                return;
            }
            float newAngle = MathUtils.latLngToAngle(position.getVlat(),
                    position.getVlng());
            //我超前新数据同时我可以等他一会
            if (MathUtils.amIFaster(traceCtrl.crtLat, traceCtrl.crtLng, traceCtrl.rotate,
                    position.getLat(), position.getLng()) &&
                    Math.abs(traceCtrl.rotate - newAngle) < 45) {
                Log.d(tag, "dispatchNewTraceCtrl: 超前 等待中... ");
                builder.clear();
                sendMessage(createHoldMessage(newAngle, builder.setLat(traceCtrl.crtLat)
                        .setLng(traceCtrl.crtLng).build(), WAIT_TIME));
                position.setLat(position.getLat() + position.getVlat() * WAIT_TIME / 1000);
                position.setLng(position.getLng() + position.getVlng() * WAIT_TIME / 1000);
                return;
            }
            //我落后新数据
            double distance = new Line(traceCtrl.crtLng, traceCtrl.crtLat,
                    position.getLng(), position.getLat()).getLength();
            double speed = new VectorD(position.getVlng(), position.getVlat()).mag();
            double time = distance / speed * 1000;
            if(time > MAX_PURSUE_TIME) time = MAX_PURSUE_TIME;
            Log.d(tag, "dispatchNewTraceCtrl: 追赶... " + (long) time + "ms");
            builder.clear();
            Message normalMessage = createNormalMessage(traceCtrl.rotate,
                    builder.setLat(traceCtrl.crtLat).setLng(traceCtrl.crtLng)
                            .setTime(position.getTime() - (long) time).build(), position);
            sendMessage(normalMessage);
            return;
        }
        //开始下一段动画
        float rftRotate = traceCtrl.getCurrentRotate();
        GpsSampleRsp gpsSampleRsp = data.takeFirst();
        if (gpsSampleRsp == null) {
            Log.e(tag, "dispatchNewTraceCtrl: 开始下一段动画: 没有数据");
            return;
        }
        Log.d(tag, "dispatchNewTraceCtrl: data = " + gpsSampleRsp.getTime());
        sendMessage(data.isEmpty() ? createInfMessage(rftRotate, gpsSampleRsp) :
                createNormalMessage(rftRotate, gpsSampleRsp, data.getFirst()));
    }

    protected void requestInf(double lat, double lng) {

    }

    protected void requestNormal(double lat, double lng) {

    }

    private Message createMessage(TraceCtrl traceCtrl) {
        Message message = Message.obtain();
        message.what = (int) carID;
        message.obj = traceCtrl;
        Log.d(TAG.ME, "createMassage: create traceCtrl");
        return message;
    }

    private Message createHoldMessage(float refRotate, GpsSampleRsp point, long time) {
        Message message = Message.obtain();
        HoldPositionCtrl hold = factory.hold().init(refRotate, point, time);
        message.obj = hold;
        message.what = (int) carID;
        Log.d(TAG.ME, "createHoldMessage: create hold " + hold.id);
        requestInf(point.getLat(), point.getLng());
        return message;
    }

    private Message createInfMessage(float refRotate, GpsSampleRsp point) {
        Message message = Message.obtain();
        InfiniteTraceCtrl infinite = factory.infinite().init(refRotate, point);
        message.obj = infinite;
        message.what = (int) carID;
        Log.d(TAG.ME, "createInfMessage: create infinite " + infinite.id);
        requestInf(point.getLat(),point.getLng());
        return message;
    }

    private Message createNormalMessage(float refRotate, GpsSampleRsp from, GpsSampleRsp to) {
        Message message = Message.obtain();
        NormalTraceCtrl normal = factory.normal().init(refRotate, from, to);
        message.obj = normal;
        message.what = (int) carID;
        Log.d(TAG.ME, "createNormalMessage: create normal " + normal.id);
        requestNormal(from.getLat(), from.getLng());
        return message;
    }

    @Override
    public void onReceiveDataWhenListIsEmpty() {
        post(new Runnable() {
            @Override
            public void run() {
                onReceiveDataWhenListIsEmptyThreadSafe();
            }
        });
    }

    public void onReceiveDataWhenListIsEmptyThreadSafe() {
        Log.d(tag, "onReceiveDataWhenListIsEmptyThreadSafe: ");
        if (data.isEmpty()) {
            Log.d(TAG.ME, "onReceiveDataWhenListIsEmptyThreadSafe :没数据？在逗我？");
            return;
        }
        currentTrace.recycle();
        //在静态模式下 对于无限远动画需立即做切换处理
        if (currentAnimationState == STATE_STATIC &&
                currentTrace instanceof InfiniteTraceCtrl) {
            removeMessages((int) carID);
            Message message = createInfMessage(currentTrace.rotate, data.takeFirst());
            sendMessage(message);
        }
    }

    public void setAnimationState(int state) {
        if (currentAnimationState == state) return;
        currentAnimationState = state;
        if (currentAnimationState == STATE_DYNAMIC) {
            Message message = Message.obtain();
            message.arg1 = ARG_RESUME;
            sendMessage(message);
        }
    }

    public long getCarID() {
        return carID;
    }

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    public void destroy() {
        destroy = true;
        removeMessages((int) carID);
        if (currentTrace != null) currentTrace.recycle();
        if (marker != null) marker.remove();
        if (data != null) {
            data.setCallback(null);
            data.clear();
        }
    }

    private static LatLng toLatLnt(GpsSampleRsp gps) {
        return new LatLng(gps.getLat(), gps.getLng());
    }

}
