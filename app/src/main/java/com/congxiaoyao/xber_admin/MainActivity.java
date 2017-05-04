package com.congxiaoyao.xber_admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.httplib.request.UserRequest;
import com.congxiaoyao.httplib.request.body.User;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.xber_admin.databinding.ActivityMainBinding;
import com.congxiaoyao.xber_admin.dispatch.DispatchTaskActivity;
import com.congxiaoyao.xber_admin.driverslist.DriverListActivity;
import com.congxiaoyao.xber_admin.helpers.MapActivityHelper;
import com.congxiaoyao.xber_admin.helpers.NavigationHelper;
import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.monitoring.XberMonitorMapFragment;
import com.congxiaoyao.xber_admin.publishedtask.PublishedTaskActivity;
import com.congxiaoyao.xber_admin.service.StompService;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.settings.Settings;
import com.congxiaoyao.xber_admin.settings.SettingsActivity;
import com.congxiaoyao.xber_admin.spotmanage.SpotManageActivity;
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;
import com.congxiaoyao.xber_admin.utils.Token;
import com.congxiaoyao.xber_admin.widget.LoadingLayout;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

public class MainActivity extends StompBaseActivity {

    public static final String KEY_BOUNDS = "KEY_BOUNDS";
    public static final String ACTION_MOVE_MAP = "ACTION_MOVE_MAP";

    private NavigationHelper helper;
    private ActivityMainBinding binding;
    private TopBarPagerAdapter pagerAdapter;

    private XberMonitorMapFragment monitorFragment;
    private Admin admin;

    private List<Runnable> fuckingRunnable = new ArrayList<>();

    private BroadcastReceiver moveMapReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LatLngBounds bounds = intent.getParcelableExtra(KEY_BOUNDS);
            TextureMapView mapView = monitorFragment.getMapView();
            if (mapView != null && bounds != null) {
                BaiduMapUtils.moveToBoundsAnimate(mapView.getMap(), bounds,
                        mapView.getWidth(), mapView.getHeight());
            }
        }
    };

    public static void moveMap(Context context, LatLngBounds bounds) {
        Intent intent = new Intent(ACTION_MOVE_MAP);
        intent.putExtra(KEY_BOUNDS, bounds);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long pre = System.currentTimeMillis();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Log.d("cxy", "binding time = " + (System.currentTimeMillis() - pre));

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
        helper.getHeaderView().findViewById(R.id.img_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        Log.d("cxy", "NavigationHelper time = " + (System.currentTimeMillis() - pre));

        MapActivityHelper.showStatusBar(binding.statusBar);
        Log.d("cxy", "showStatusBar time = " + (System.currentTimeMillis() - pre));

        pagerAdapter = new TopBarPagerAdapter(binding.animationLayer,
                binding.topBarPager);
        binding.topBarPager.setAdapter(pagerAdapter);
        binding.topBarPager.addOnPageChangeListener(pagerAdapter.
                new PageScrollHelper(binding.topBarPager));

        pagerAdapter.getSearchCarBar().setupWithDrawerLayout(binding.drawerLayout);
        pagerAdapter.getSearchAddrBar().setupWithDrawerLayout(binding.drawerLayout);
        pagerAdapter.setOnTraceCarListener(new TopBarPagerAdapter.OnTraceCarListener() {
            @Override
            public void onTraceCar(List<Long> carIds, LatLngBounds latLngBounds) {
                if (carIds == null) {
                    onTraceAllCar();
                } else {
                    onTraceSpecifiedCar(carIds,latLngBounds);
                }
            }
        });
        Log.d("cxy", "pagerAdapter time = " + (System.currentTimeMillis() - pre));

        binding.loadingLayout.below(R.id.top_bar_pager, 16);
        Log.d("cxy", "loadingLayout time = " + (System.currentTimeMillis() - pre));

        monitorFragment = XberMonitorMapFragment
                .newInstance(new StompServiceProvider() {
                    @Override
                    public StompService getService() {
                        return stompService;
                    }
                });
        LocalBroadcastManager.getInstance(this).registerReceiver(moveMapReceiver,
                new IntentFilter(ACTION_MOVE_MAP));

        getSupportFragmentManager().beginTransaction().replace(R.id.map_container,
                monitorFragment).commit();
        Log.d("cxy", "on create time = " + (System.currentTimeMillis() - pre));

    }

    /**
     * 如果没通过检查 返回false
     * 返回false将会自动延时重试
     * @param runnable
     * @return
     */
    private boolean checkAndRetry(Runnable runnable) {
        if (monitorFragment == null || monitorFragment.getBaiduMap() == null
                || monitorFragment.getMonitor() == null) {
            fuckingRunnable.add(runnable);
            binding.getRoot().postDelayed(runnable, 1000);
            return false;
        }
        return true;
    }

    /**
     * 两个topic都准备好了 可以开始请求数据了
     */
    @Override
    protected void onStompPrepared() {
        boolean pass = checkAndRetry(new Runnable() {
            @Override
            public void run() {
                onStompPrepared();
            }
        });
        if (!pass) return;
        pagerAdapter.setEnabled(true);
        onTraceAllCar();
    }

    @Override
    public void onCarAdd(final long carId, final SyncOrderedList<GpsSampleRsp> trace) {
        boolean pass = checkAndRetry(new Runnable() {
            @Override
            public void run() {
                onCarAdd(carId, trace);
            }
        });
        if (!pass) return;
        monitorFragment.getMonitor().onCarAdd(carId, trace);
    }

    @Override
    public void onCarRemove(final long carId) {
        boolean pass = checkAndRetry(new Runnable() {
            @Override
            public void run() {
                onCarRemove(carId);
            }
        });
        if (!pass) return;
        monitorFragment.getMonitor().onCarRemove(carId);
    }

    public void onTraceAllCar() {
        boolean pass = checkAndRetry(new Runnable() {
            @Override
            public void run() {
                onTraceAllCar();
            }
        });
        if (!pass) return;
        monitorFragment.getMonitor().onTraceAllCar();
    }

    public void onTraceSpecifiedCar(final List<Long> carIds, final LatLngBounds latLngBounds) {
        boolean pass = checkAndRetry(new Runnable() {
            @Override
            public void run() {
                onTraceSpecifiedCar(carIds, latLngBounds);
            }
        });
        if (!pass) return;
        monitorFragment.getMonitor().onTraceSpecifiedCar(carIds, latLngBounds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Token.value == null || Token.value.length() == 0) {
            ((TextView) helper.getHeaderView().findViewById(R.id.tv_user_name))
                    .setText("点击登陆/注册");
        }
    }

    @Override
    protected void tokenSafeOnResume(Admin admin) {
        this.admin = admin;
        super.tokenSafeOnResume(admin);
        if (monitorFragment != null) monitorFragment.tokenSafeOnResume();
        ((TextView) helper.getHeaderView().findViewById(R.id.tv_user_name))
                .setText(admin.getNickName());
        Log.d(TAG.ME, "tokenSafeOnResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG.ME, "onPause: ");
        unBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG.ME, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(moveMapReceiver);
        if (pagerAdapter != null) {
            pagerAdapter.destroy();
        }
        if (binding != null) {
            for (Runnable runnable : fuckingRunnable) {
                binding.getRoot().removeCallbacks(runnable);
            }
            Log.d(TAG.ME, "onDestroy: clear runnables " + fuckingRunnable.size());
            fuckingRunnable.clear();
            binding.unbind();
            binding = null;
        }
    }

    @Override
    protected LoadingLayout getLoadingLayout() {
        return binding.loadingLayout;
    }

    public void onItemSelected(int menuId) {
        if (menuId == R.id.menu_car_monitor) {
            binding.drawerLayout.closeDrawers();
            return;
        }
        if (menuId == R.id.menu_task_send) {
            startActivity(new Intent(this, DispatchTaskActivity.class));
        } else if (menuId == R.id.menu_drivers) {
            startActivity(new Intent(this, DriverListActivity.class));
        } else if (menuId == R.id.menu_spot) {
            startActivity(new Intent(this, SpotManageActivity.class));
        } else if (menuId == R.id.menu_task_has_sent) {
            startActivity(new Intent(this,PublishedTaskActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (pagerAdapter == null ||!pagerAdapter.onBackPressed()) {
            super.onBackPressed();
        }
    }

}
