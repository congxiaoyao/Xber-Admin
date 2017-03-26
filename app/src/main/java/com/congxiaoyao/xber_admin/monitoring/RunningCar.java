package com.congxiaoyao.xber_admin.monitoring;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.monitoring.model.NormalTraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class RunningCar extends Handler implements SyncOrderedList.DataReceiveListener{

    public static final String KEY_CAR_ID = "CAR_ID";

    private long carID;
    private final Marker marker;
    private SyncOrderedList<GpsSampleRsp> data;
    private TraceCtrlFactory factory;

    private RunningCar(TraceCtrlFactory factory, BaiduMap map, SyncOrderedList<GpsSampleRsp> data) {
        this.factory = factory;
        this.data = data;
        if (data == null || data.size() == 0) {
            throw new RuntimeException("上来给了我一个空的list?? 是不是出错了");
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_red_car));
        GpsSampleRsp gpsSampleRsp = data.takeFirst();
        markerOptions.position(toLatLnt(gpsSampleRsp));
        marker = (Marker) map.addOverlay(markerOptions);
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_CAR_ID, carID = gpsSampleRsp.getCarId());
        marker.setExtraInfo(bundle);

        Message message;
        if (data.isEmpty()) {
            message = createNewMessage(gpsSampleRsp);
        } else {
            message = createNewMessage(gpsSampleRsp, data.getFirst());
        }
        sendMessage(message);

        data.setCallback(this);
    }

    private Message createNewMessage(GpsSampleRsp start, GpsSampleRsp end) {
        Message message = Message.obtain();
        message.what = (int) start.getCarId();
        NormalTraceCtrl normal = factory.normal().init(start, end);
        message.obj = normal;
        return message;
    }

    private Message createNewMessage(GpsSampleRsp gpsSampleRsp) {
        return null;
    }

    @Override
    public void handleMessage(Message msg) {
        TraceCtrl traceCtrl = (TraceCtrl) msg.obj;
        if (traceCtrl.shouldStop()) {
            //TODO do something then return
            traceCtrl.recycle();
            return;
        }
		traceCtrl.handleAndPost(this);
        marker.setPosition(new LatLng(traceCtrl.crtLat, traceCtrl.crtLng));
        marker.setRotate(traceCtrl.rotate);
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

    }

    private static LatLng toLatLnt(GpsSampleRsp gps) {
        return new LatLng(gps.getLat(), gps.getLng());
    }
}
