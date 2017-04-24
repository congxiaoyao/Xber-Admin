package com.congxiaoyao.xber_admin.monitoring;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
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
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import java.util.ArrayList;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class RunningCar extends Handler implements SyncOrderedList.DataReceiveListener{

    public static final String KEY_CAR_ID = "CAR_ID";
    public static final String tag = RunningCar.class.getSimpleName();
    public static final int WAIT_TIME = 1000;

    private long carID;
    private final Marker marker;
    private SyncOrderedList<GpsSampleRsp> data;
    private TraceCtrlFactory factory;

    private TraceCtrl currentTrace;
    private TextureMapView mapView;

    public void setMapView(TextureMapView mapView) {
        this.mapView = mapView;
    }

    private GpsSampleRsp.Builder builder = GpsSampleRsp.newBuilder();

    public RunningCar(TraceCtrlFactory factory, BaiduMap map, SyncOrderedList<GpsSampleRsp> data) {
        super();
        this.factory = factory;
        this.data = data;
        if (data == null || data.size() == 0) {
            throw new RuntimeException("上来给了我一个空的list?? 是不是出错了");
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_red_car));
        GpsSampleRsp gpsSampleRsp = data.takeFirst();
        markerOptions.position(toLatLnt(gpsSampleRsp));
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
        TraceCtrl traceCtrl = (TraceCtrl) msg.obj;
        //此段轨迹动画播放结束
        if (traceCtrl.shouldStop()) {
            dispatchNewTraceCtrl(traceCtrl);
            return;
        }
        currentTrace = traceCtrl;
        //计算这一帧的位置 并发送延时消息准备计算下一帧
        traceCtrl.calculateAndPost(this);

        //根据计算结果更新位置
        LatLng latLng = traceCtrl.getCurrentLatLng();
        marker.setPosition(latLng);
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
            float newAngle = MathUtils.latLngToAngle(position.getVlat(),
                    position.getVlng());
            //我超前新数据同时我可以等他一会
            if (MathUtils.amIFaster(traceCtrl.crtLat, traceCtrl.crtLng, traceCtrl.rotate,
                    position.getLat(), position.getLng()) &&
                    Math.abs(traceCtrl.rotate - newAngle) < 60) {
                Log.d(tag, "dispatchNewTraceCtrl: 超前 等待中...");
                sendMessage(createHoldMessage(newAngle, builder.setLat(traceCtrl.crtLat)
                        .setLng(traceCtrl.crtLng).build(), WAIT_TIME));
                return;
            }
            //我落后新数据
            double distance = new Line(traceCtrl.crtLng, traceCtrl.crtLat,
                    position.getLng(), position.getLat()).getLength();
            double speed = new VectorD(position.getVlng(), position.getVlat()).mag();
            double time = distance / speed * 1000;
            if(time > 5000) time = 5000;
            Log.d(tag, "dispatchNewTraceCtrl: 追赶... " + (long) time + "ms");
            Message normalMessage = createNormalMessage(traceCtrl.rotate,
                    builder.setLat(traceCtrl.crtLat).setLng(traceCtrl.crtLng)
                            .setTime(position.getTime() - (long) time).build(), position);
            sendMessage(normalMessage);
            return;
        }
        //开始下一段动画
        float rftRotate = traceCtrl.getCurrentRotate();
        GpsSampleRsp gpsSampleRsp = data.takeFirst();
        Log.d(tag, "dispatchNewTraceCtrl: data = " + gpsSampleRsp.getTime());
        sendMessage(data.isEmpty() ? createInfMessage(rftRotate, gpsSampleRsp) :
                createNormalMessage(rftRotate, gpsSampleRsp, data.getFirst()));
    }

    protected void requestInf(double lat, double lng) {

    }

    protected void requestNormal(double lat, double lng) {

    }

    private Message createHoldMessage(float refRotate, GpsSampleRsp point, long time) {
        Message message = Message.obtain();
        message.what = (int) point.getCarId();
        HoldPositionCtrl hold = factory.hold().init(refRotate, point, time);
        message.obj = hold;
        Log.d(TAG.ME, "createHoldMessage: create hold " + hold.id);
        requestInf(point.getLat(), point.getLng());
        return message;
    }

    private Message createInfMessage(float refRotate, GpsSampleRsp point) {
        Message message = Message.obtain();
        message.what = (int) point.getCarId();
        InfiniteTraceCtrl infinite = factory.infinite().init(refRotate, point);
        message.obj = infinite;
        Log.d(TAG.ME, "createInfMessage: create infinite " + infinite.id);
        requestInf(point.getLat(),point.getLng());
        return message;
    }

    private Message createNormalMessage(float refRotate, GpsSampleRsp from, GpsSampleRsp to) {
        Message message = Message.obtain();
        message.what = (int) from.getCarId();
        NormalTraceCtrl normal = factory.normal().init(refRotate, from, to);
        message.obj = normal;
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
    }

    private static LatLng toLatLnt(GpsSampleRsp gps) {
        return new LatLng(gps.getLat(), gps.getLng());
    }

    public long getCarID() {
        return carID;
    }

    public void destroy() {
        currentTrace.recycle();
        marker.remove();
        data.clear();
        removeMessages((int) carID);
    }
}
