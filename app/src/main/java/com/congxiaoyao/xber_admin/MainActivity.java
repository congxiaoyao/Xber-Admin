package com.congxiaoyao.xber_admin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.congxiaoyao.Admin;
import com.congxiaoyao.xber_admin.databinding.ActivityMainBinding;

import android.databinding.ViewDataBinding;
import com.congxiaoyao.xber_admin.dispatch.DispatchTaskActivity;
import com.congxiaoyao.xber_admin.helpers.NavigationHelper;
import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.Token;
import com.congxiaoyao.xber_admin.utils.VersionUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private NavigationHelper helper;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
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
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG.ME, "onCreate: " + android_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                Log.d(TAG.ME, "call: "+Token.value);
                ((TextView) helper.getHeaderView().findViewById(R.id.tv_user_name))
                        .setText(admin.getNickName());
                tokenSafeOnResume();
            }
        });
    }

    private void tokenSafeOnResume() {

    }

    public void onItemSelected(int menuId) {
        if (menuId == R.id.menu_car_monitor) {
            binding.drawerLayout.closeDrawers();
        } else if (menuId == R.id.menu_drivers) {
            startActivity(new Intent(this, WheelTestActivity.class));
        } else if (menuId == R.id.menu_task_send) {
            startActivity(new Intent(this, DispatchTaskActivity.class));
        }
    }
}
