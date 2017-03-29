package com.congxiaoyao.xber_admin.monitoring;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.StompBaseActivity.StompServiceProvider;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;

import java.util.List;
import java.util.Objects;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class XberMonitor implements ISearchBarState,IStompState,IMapState {

    private TextureMapView mapView;
    private BaiduMap baiduMap;
    private StompServiceProvider serviceProvider;
    private TraceCtrlFactory factory;

    public XberMonitor(TextureMapView mapView, BaiduMap baiduMap,
                       StompServiceProvider serviceProvider) {
        Objects.requireNonNull(mapView);
        Objects.requireNonNull(baiduMap);
        Objects.requireNonNull(serviceProvider);
        this.mapView = mapView;
        this.baiduMap = baiduMap;
        this.serviceProvider = serviceProvider;
        this.factory = new TraceCtrlFactory(10, 10, 10);
    }

    @Override
    public void onTraceAllCar() {

    }

    @Override
    public void onTraceSpecifiedCar(List<Long> carIds) {

    }

    @Override
    public void onCarAdd(long carId, SyncOrderedList<GpsSampleRsp> trace) {

    }

    @Override
    public void onCarRemove(long carId) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_red_car));
        markerOptions.position(latLng).anchor(0.5f, 0.5f);
//        BaiduMapUtils.moveToLatLng(baiduMap, latLng.latitude, latLng.longitude, 13);
        final Marker car = (Marker) baiduMap.addOverlay(markerOptions);
        new HandlerThread("abc"){
            @Override
            protected void onLooperPrepared() {
                Handler handler = new Handler(getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1000) return;
                        LatLng data = (LatLng) msg.obj;
                        car.setPosition(data);
                        double lng = data.longitude + 0.00002;
                        data = new LatLng(data.latitude, lng);
                        msg.what++;
                        msg.obj = data;
                        Message obtain = Message.obtain();
                        obtain.what = msg.what;
                        obtain.obj = msg.obj;
                        sendMessageDelayed(obtain, 16);
                    }
                };
                Message message = Message.obtain();
                message.what = 0;
                message.obj = latLng;
                handler.sendMessage(message);
            }
        }.start();
    }

    public void close() {
        factory.clear();
    }

    private static long getCarId(Marker marker) {
        final Bundle bundle = marker.getExtraInfo();
        return bundle.getLong(RunningCar.KEY_CAR_ID);
    }
}
