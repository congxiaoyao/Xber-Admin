package com.congxiaoyao.xber_admin.monitoring;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_admin.StompBaseActivity;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;

/**
 * Created by congxiaoyao on 2017/4/6.
 */

public class XberMonitorMapFragment extends Fragment {

    private TextureMapView mapView;
    private XberMonitor monitor;
    private StompBaseActivity.StompServiceProvider stompProvider;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        mapView = new TextureMapView(getActivity());
        configBaiduMap(mapView.getMap());
        return mapView;
    }

    private void configBaiduMap(final BaiduMap baiduMap) {
        UiSettings uiSettings = baiduMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        baiduMap.showMapIndoorPoi(false);
        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                monitor.onMapStatusChangeFinish(mapStatus);
            }
        });

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (monitor != null) monitor.onMarkerClick(marker);
                return true;
            }
        });

        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (monitor != null) monitor.onMapClick(latLng);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                BaiduMapUtils.moveToLatLng(baiduMap, 39.066252, 117.147011);
            }
        });
    }

    public static XberMonitorMapFragment newInstance(StompBaseActivity.StompServiceProvider provider) {
        XberMonitorMapFragment fragment = new XberMonitorMapFragment();
        fragment.stompProvider = provider;
        return fragment;
    }

    public XberMonitor getMonitor() {
        return monitor;
    }

    public TextureMapView getMapView() {
        return mapView;
    }

    public BaiduMap getBaiduMap() {
        return mapView == null ? null : mapView.getMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        monitor = new XberMonitor(mapView, mapView.getMap(), stompProvider);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (monitor != null) {
            monitor.close();
            monitor = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (monitor != null) {
            monitor.close();
            monitor = null;
        }
    }
}