package com.congxiaoyao.xber_admin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.congxiaoyao.Admin;
import com.congxiaoyao.TopBarPagerAdapter;
import com.congxiaoyao.xber_admin.databinding.ActivityMainBinding;
import com.congxiaoyao.xber_admin.dispatch.DispatchTaskActivity;
import com.congxiaoyao.xber_admin.helpers.NavigationHelper;
import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.Token;
import com.congxiaoyao.xber_admin.utils.VersionUtils;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private NavigationHelper helper;
    private ActivityMainBinding binding;
    private TopBarPagerAdapter pagerAdapter;
    private BaiduMap baiduMap;

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
                    Log.d(TAG.ME, "onTraceCar: null");
                }else {
                    Log.d(TAG.ME, "onTraceCar: " + carIds);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
        Observable.just(1).map(new Func1<Integer, Admin>() {
            @Override
            public Admin call(Integer integer) {
                Admin admin = Admin.fromSharedPreference(MainActivity.this);
                return admin;
            }
        }).subscribeOn(Schedulers.io()).filter(new Func1<Admin, Boolean>() {
            @Override
            public Boolean call(Admin admin) {
                return admin != null;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Admin>() {
            @Override
            public void call(Admin admin) {
                Token.value = admin.getToken();
                ((TextView) helper.getHeaderView().findViewById(R.id.tv_user_name))
                        .setText(admin.getNickName());
                tokenSafeOnResume();
            }
        });
    }

    private void tokenSafeOnResume() {
        Log.d(TAG.ME, "token = " + Token.value);
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

    public void onItemSelected(int menuId) {
        if (menuId == R.id.menu_car_monitor) {
            binding.drawerLayout.closeDrawers();
        }  else if (menuId == R.id.menu_task_send) {
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
