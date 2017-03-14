package com.congxiaoyao.xber_admin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.congxiaoyao.xber_admin.databinding.ActivityMainBinding;
import com.congxiaoyao.xber_admin.helpers.NavigationHelper;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        NavigationHelper helper = new NavigationHelper(binding.navView,
                R.menu.navigation, R.layout.nav_main, R.layout.nav_header);
        helper.onItemSelected(new Action1<Integer>() {
            @Override
            public void call(Integer id) {
                onItemSelected(id);
            }
        });
    }

    public void onItemSelected(int menuId) {
        if (menuId == R.id.menu_drivers) {
            Toast.makeText(MainActivity.this, "menu drivers", Toast.LENGTH_SHORT).show();
        }
    }
}
