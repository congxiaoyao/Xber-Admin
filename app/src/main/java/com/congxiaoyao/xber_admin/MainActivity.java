package com.congxiaoyao.xber_admin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.Admin;
import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.xber_admin.databinding.ActivityMainBinding;
import com.congxiaoyao.xber_admin.dispatch.DispatchTaskActivity;
import com.congxiaoyao.xber_admin.helpers.NavigationHelper;
import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.monitoring.XberMonitor;
import com.congxiaoyao.xber_admin.service.StompService;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.VersionUtils;
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;
import com.congxiaoyao.xber_admin.widget.LoadingLayout;

import java.util.List;

import rx.functions.Action1;

public class MainActivity extends StompBaseActivity {

    private NavigationHelper helper;
    private ActivityMainBinding binding;
    private TopBarPagerAdapter pagerAdapter;
    private BaiduMap baiduMap;

    private XberMonitor monitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        baiduMap = binding.mapView.getMap();
        helper = new NavigationHelper(binding.navView,
                R.menu.navigation, R.layout.nav_main, R.layout.nav_header);
        helper.onItemSelected(new Action1<Integer>() {
            @Override
            public void call(Integer id) {
                onItemSelected(id);
            }
        });
        helper.getHeaderView().findViewById(R.id.ll_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        if (VersionUtils.LOLLIPOP_MR1AndPlus) {
            View statusBar = binding.statusBar;
            statusBar.setVisibility(View.VISIBLE);
            statusBar.setBackgroundColor(Color.parseColor("#55000000"));
            ViewGroup.LayoutParams layoutParams = statusBar.getLayoutParams();
            layoutParams.height = DisplayUtils.getStatusBarHeight(this);
            statusBar.requestLayout();
        }

        pagerAdapter = new TopBarPagerAdapter(binding.animationLayer,
                binding.topBarPager);
        binding.topBarPager.setAdapter(pagerAdapter);
        binding.topBarPager.addOnPageChangeListener(pagerAdapter.
                new PageScrollHelper(binding.topBarPager));

        pagerAdapter.getSearchCarBar().setupWithDrawerLayout(binding.drawerLayout);
        pagerAdapter.getSearchAddrBar().setupWithDrawerLayout(binding.drawerLayout);
        pagerAdapter.setOnTraceCarListener(new TopBarPagerAdapter.OnTraceCarListener() {
            @Override
            public void onTraceCar(List<Long> carIds) {
                if (carIds == null) {
                    onTraceAllCar();
                } else {
                    onTraceSpecifiedCar(carIds);
                }
            }
        });
        binding.loadingLayout.below(R.id.top_bar_pager, 16);

        monitor = new XberMonitor(binding.mapView, baiduMap, new StompServiceProvider() {
            @Override
            public StompService getService() {
                return stompService;
            }
        });
        configBaiduMap();
    }

    private void configBaiduMap() {
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
                return monitor.onMarkerClick(marker);
            }
        });

        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                monitor.onMapClick(latLng);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        BaiduMapUtils.moveToLatLng(baiduMap, 39.066252, 117.147011);
    }

    @Override
    protected void onStompPrepared() {
        pagerAdapter.setEnabled(true);
        LatLng latLng = BaiduMapUtils.getScreenCenterLatLng(this, baiduMap);
        double radius = BaiduMapUtils.getScreenRadius(this, baiduMap);
        stompService.nearestNTrace(latLng.latitude, latLng.longitude, radius, 100);
    }

    @Override
    public void onCarAdd(long carId, SyncOrderedList<GpsSampleRspOuterClass.GpsSampleRsp> trace) {
        monitor.onCarAdd(carId, trace);
    }

    @Override
    public void onCarRemove(long carId) {
        monitor.onCarRemove(carId);
    }

    public void onTraceAllCar() {
        monitor.onTraceAllCar();
    }

    public void onTraceSpecifiedCar(List<Long> carIds) {
        monitor.onTraceSpecifiedCar(carIds);
    }

    @Override
    protected void onResume() {
        binding.mapView.onResume();
        super.onResume();
    }

    @Override
    protected void tokenSafeOnResume(Admin admin) {
        super.tokenSafeOnResume(admin);
        ((TextView) helper.getHeaderView().findViewById(R.id.tv_user_name))
                .setText(admin.getNickName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    protected LoadingLayout getLoadingLayout() {
        return binding.loadingLayout;
    }

    public void onItemSelected(int menuId) {
        if (menuId == R.id.menu_car_monitor) {
            binding.drawerLayout.closeDrawers();
        } else if (menuId == R.id.menu_task_send) {
            startActivity(new Intent(this, DispatchTaskActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (pagerAdapter == null) {
            super.onBackPressed();
            return;
        }
        if (!pagerAdapter.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
