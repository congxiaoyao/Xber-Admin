package com.congxiaoyao.xber_admin.monitoring;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.location.utils.Line;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.StompBaseActivity.StompServiceProvider;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.monitoring.carinfo.CarInfoPresenter;
import com.congxiaoyao.xber_admin.monitoring.carinfo.CarInfoView;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;

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

import dagger.Component;
import dagger.Module;
import dagger.Provides;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;
import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.registerAllExtensions;
import static com.congxiaoyao.xber_admin.settings.Settings.*;
import static com.congxiaoyao.xber_admin.monitoring.RunningCar.STATE_DYNAMIC;
import static com.congxiaoyao.xber_admin.monitoring.RunningCar.STATE_STATIC;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

@Module
public class XberMonitor implements ISearchBarState,IStompState,IMapState {

    //当地图缩放等级大于等于16的时候对屏幕内的车辆做动画
    public static final int MIN_ANIMATE_ZOOM = 15;
    public static final double MIN_QUERY_RADIUS = 0.02;

    private TextureMapView mapView;
    private BaiduMap baiduMap;
    private StompServiceProvider serviceProvider;
    private TraceCtrlFactory factory;

    private Map<Long, RunningCar> runningCars = new HashMap<>();
    private CarInfoPresenter presenter;

    private int maxCarCount = R.integer.def_max_car_count;

    private int animateState = STATE_DYNAMIC;

    public XberMonitor(TextureMapView mapView, BaiduMap baiduMap,
                       StompServiceProvider serviceProvider) {
        Objects.requireNonNull(mapView);
        Objects.requireNonNull(baiduMap);
        Objects.requireNonNull(serviceProvider);
        this.mapView = mapView;
        this.baiduMap = baiduMap;
        this.serviceProvider = serviceProvider;
        this.factory = new TraceCtrlFactory(10, 10, 10);
        animateState = getAnimateStateByZoomLevel(BaiduMapUtils.getZoomLevel(baiduMap));
        maxCarCount = SettingsHelper.getInstance(mapView.getContext()).maxCarCount();

    }

    /**
     * 搜索栏中要求搜索所有车辆
     */
    @Override
    public void onTraceAllCar() {
        Context context = mapView.getContext();
        LatLng latLng = BaiduMapUtils.getScreenCenterLatLng(context, baiduMap);
        double radius = BaiduMapUtils.getScreenRadius(context, baiduMap);
        if (radius < MIN_QUERY_RADIUS) radius = MIN_QUERY_RADIUS;
        serviceProvider.getService().nearestNTrace(latLng.latitude,
                latLng.longitude, radius, maxCarCount);
    }

    /**
     * 搜索栏中要求搜索指定车辆
     * @param carIds
     */
    @Override
    public void onTraceSpecifiedCar(List<Long> carIds, LatLngBounds latLngBounds) {
        if (carIds != null || carIds.size() != 0) {
            serviceProvider.getService().specifiedCarsTrace(carIds);
        }
        if (latLngBounds != null) {
            BaiduMapUtils.moveToBoundsAnimate(baiduMap, latLngBounds,
                    mapView.getWidth(), mapView.getHeight());
        }
    }

    @Override
    public void onCarAdd(long carId, SyncOrderedList<GpsSampleRsp> trace) {
        Log.d(XberMonitor.class.getSimpleName(), "onCarAdd: ");
        RunningCar runningCar = runningCars.get(carId);
        if (runningCar != null) {
            Log.e(TAG.ME, "onCarAdd: 按说这里不应该查到东西的！！");
            runningCar.destroy();
        }
        try {
            int animate = getAnimateStateByZoomLevel(BaiduMapUtils.getZoomLevel(baiduMap));
            runningCar = new RunningCar(this, trace, animate);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        runningCars.put(carId, runningCar);
    }

    private int getAnimateStateByZoomLevel( float zoomLevel) {
        return zoomLevel >= MIN_ANIMATE_ZOOM ? STATE_DYNAMIC : STATE_STATIC;
    }

    @Override
    public void onCarRemove(long carId) {
        Log.d(XberMonitor.class.getSimpleName(), "onCarRemove: ");
        RunningCar removed = runningCars.remove(carId);
        if (removed != null) {
            removed.destroy();
        }
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        int state = getAnimateStateByZoomLevel(mapStatus.zoom);
        //地图状态发生变化 切换车辆动画状态
        Set<Long> carIds = runningCars.keySet();
        for (Long carId : carIds) {
            RunningCar runningCar = runningCars.get(carId);
            //如果当前缩放状态需要动画 则对屏幕内的所有车设置为动画状态
            if (state == STATE_DYNAMIC &&
                    mapStatus.bound.contains(runningCar.getCurrentLatLng())) {
                runningCar.setAnimationState(STATE_DYNAMIC);
            }
            //不在屏幕范围内的设置为静态
            else {
                runningCar.setAnimationState(STATE_STATIC);
            }
        }
        //地图状态发生变化 考虑重新提交请求参数
        //当搜索指定车辆时 不需要提交新的请求参数
        if (serviceProvider.getService().isSpecifiedCarsRunning()) {
            return;
        }
        //否则重新请求屏幕范围内的车辆
        LatLngBounds bound = mapStatus.bound;
        LatLng latLng = bound.getCenter();
        LatLng ne = bound.northeast;
        LatLng sw = bound.southwest;
        Line line = new Line(ne.longitude, ne.latitude, sw.longitude, sw.latitude);
        double radius = line.getLength() / 2;
        if (radius < MIN_QUERY_RADIUS) radius = MIN_QUERY_RADIUS;
        serviceProvider.getService().nearestNTrace(latLng.latitude,
                latLng.longitude, radius, maxCarCount);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        long carId = getCarId(marker);
        if (carId != 0) {
            presenter = new CarInfoPresenter(new CarInfoView((Activity)
                    mapView.getContext()), carId);
        }
        return true;
    }

    private SyncOrderedList<GpsSampleRsp> testData;
    private GpsSampleRsp lastData;

    @Override
    public void onMapClick(final LatLng latLng) {
//        if (testData == null) {
//            testData = getTestData();
//            lastData = testData.getLast();
//            onCarAdd(testData.getFirst().getCarId(), testData);
//        }else {
//            lastData = GpsSampleRsp.newBuilder().setLat(latLng.latitude)
//                    .setLng(latLng.longitude)
//                    .setTime(System.currentTimeMillis())
//                    .setVlat(lastData.getVlat())
//                    .setVlng(lastData.getVlng())
//                    .setCarId(lastData.getCarId()).build();
//            testData.insert(lastData);
//        }
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
        Log.d(TAG.ME, "XberMonitor close: ");
        if (presenter != null) {
            presenter.unSubscribe();
            presenter = null;
        }
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


    @Provides
    public TextureMapView providesTextureMapView() {
        return mapView;
    }

    @Provides
    public TraceCtrlFactory providesTraceCtrlFactory() {
        return factory;
    }

    @Provides
    public BaiduMap providesBaiduMap() {
        return baiduMap;
    }

    @Component(modules = XberMonitor.class)
    public interface MonitorComponent {

        void inject(RunningCar runningCar);
    }
}
