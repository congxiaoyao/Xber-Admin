package com.congxiaoyao.xber_admin.monitoring;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.StompBaseActivity.StompServiceProvider;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.monitoring.carinfo.CarInfoPresenter;
import com.congxiaoyao.xber_admin.monitoring.carinfo.CarInfoView;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.widget.BottomDialog;

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
import static com.congxiaoyao.xber_admin.monitoring.RunningCar.STATE_DYNAMIC;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

@Module
public class XberMonitor implements ISearchBarState,IStompState,IMapState {

    private TextureMapView mapView;
    private BaiduMap baiduMap;
    private StompServiceProvider serviceProvider;
    private TraceCtrlFactory factory;

    private Map<Long, RunningCar> runningCars = new HashMap<>();
    private CarInfoPresenter presenter;

    public XberMonitor(TextureMapView mapView, BaiduMap baiduMap,
                       StompServiceProvider serviceProvider) {
        Objects.requireNonNull(mapView);
        Objects.requireNonNull(baiduMap);
        Objects.requireNonNull(serviceProvider);
        this.mapView = mapView;
        this.baiduMap = baiduMap;
        this.serviceProvider = serviceProvider;
        this.factory = new TraceCtrlFactory(10, 10, 10);

        Button button = new Button(mapView.getContext());
        button.setText("BUTTON????");
        button.setOnClickListener(new View.OnClickListener() {
            private int state = STATE_DYNAMIC;
            @Override
            public void onClick(View v) {
                state = (state + 1) % 2;
                if (state == STATE_DYNAMIC) {
                    changeToDynamic();
                }else {
                    changeToStatic();
                }
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 960;
        ((ViewGroup) mapView.getParent()).addView(button, params);

    }

    public void changeToStatic() {
        Set<Long> carIds = runningCars.keySet();
        for (Long carId : carIds) {
            RunningCar runningCar = runningCars.get(carId);
            runningCar.changeToStatic();
        }
    }

    public void changeToDynamic() {
        Set<Long> carIds = runningCars.keySet();
        for (Long carId : carIds) {
            RunningCar runningCar = runningCars.get(carId);
            runningCar.changeToDynamic();
        }
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
        try {
            runningCar = new RunningCar(this, trace, STATE_DYNAMIC);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
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
