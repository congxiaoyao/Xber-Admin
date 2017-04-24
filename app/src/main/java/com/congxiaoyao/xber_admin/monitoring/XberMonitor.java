package com.congxiaoyao.xber_admin.monitoring;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.StompBaseActivity.StompServiceProvider;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class XberMonitor implements ISearchBarState,IStompState,IMapState {

    private TextureMapView mapView;
    private BaiduMap baiduMap;
    private StompServiceProvider serviceProvider;
    private TraceCtrlFactory factory;

    private Map<Long, RunningCar> runningCars = new HashMap<>();

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
        RunningCar runningCar = runningCars.get(carId);
        if (runningCar != null) {
            Log.e(TAG.ME, "onCarAdd: 按说这里不应该查到东西的！！");
            runningCar.destroy();
        }
        runningCar = new RunningCar(factory,baiduMap,trace){
//            @Override
//            protected void requestInf(double lat, double lng) {
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_goal));
//                markerOptions.position(new LatLng(lat, lng));
//                baiduMap.addOverlay(markerOptions);
//            }
//
//            @Override
//            protected void requestNormal(double lat, double lng) {
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_start));
//                markerOptions.position(new LatLng(lat, lng));
//                baiduMap.addOverlay(markerOptions);
//            }
        };
        runningCar.setMapView(mapView);
        runningCars.put(carId, runningCar);
    }

    @Override
    public void onCarRemove(long carId) {
        RunningCar removed = runningCars.remove(carId);
        if (removed != null) {
            removed.destroy();
        }
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    private SyncOrderedList<GpsSampleRsp> testData;
    private GpsSampleRsp lastData;

    @Override
    public void onMapClick(final LatLng latLng) {

        if (testData == null) {
            testData = getTestData();
            lastData = testData.getLast();
            onCarAdd(testData.getFirst().getCarId(), testData);
        }else {
            lastData = GpsSampleRsp.newBuilder().setLat(latLng.latitude)
                    .setLng(latLng.longitude)
                    .setTime(System.currentTimeMillis())
                    .setVlat(lastData.getVlat())
                    .setVlng(lastData.getVlng())
                    .setCarId(lastData.getCarId()).build();
            testData.insert(lastData);
        }
    }

    public SyncOrderedList<GpsSampleRsp> getTestData() {
        SyncOrderedList<GpsSampleRsp> syncOrderedList =
                new SyncOrderedList<>(new Comparator<GpsSampleRsp>() {
                    @Override
                    public int compare(GpsSampleRsp o1, GpsSampleRsp o2) {
                        return Long.compare(o1.getTime(), o2.getTime());
                    }
                });
        AssetManager assets = mapView.getContext().getAssets();
        try {
            InputStream open = assets.open("animation_row_data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(open));
            String line = null;
            GpsSampleRsp.Builder builder = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("lng")) {
                    builder = GpsSampleRsp.newBuilder();
                    builder.setLng(Double.parseDouble(line.substring(line.indexOf(":") + 1)));
                } else if (line.startsWith("lat")) {
                    builder.setLat(Double.parseDouble(line.substring(line.indexOf(":") + 1)));
                } else if (line.startsWith("vlng")) {
                    builder.setVlng(Double.parseDouble(line.substring(line.indexOf(":") + 1)));
                } else if (line.startsWith("vlat")) {
                    builder.setVlat(Double.parseDouble(line.substring(line.indexOf(":") + 1)));
                } else if (line.startsWith("carId")) {
                    builder.setCarId(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                } else if (line.startsWith("time")) {
                    builder.setTime(Long.parseLong(line.substring(line.indexOf(":") + 2)));
                } else if (line.startsWith("taskId")) {
                    GpsSampleRsp build = builder.build();
                    syncOrderedList.insert(build);
                }
            }
            open.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return syncOrderedList;
    }

    public void close() {
        if (runningCars != null) {
            Set<Long> keySet = runningCars.keySet();
            for (Long carId : keySet) {
                runningCars.get(carId).destroy();
            }
        }
        runningCars.clear();
        factory.clear();
    }

    private static long getCarId(Marker marker) {
        final Bundle bundle = marker.getExtraInfo();
        return bundle.getLong(RunningCar.KEY_CAR_ID);
    }
}
