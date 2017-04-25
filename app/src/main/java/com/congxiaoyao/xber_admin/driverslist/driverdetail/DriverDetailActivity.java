package com.congxiaoyao.xber_admin.driverslist.driverdetail;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ActivityDriverDetailBinding;
import com.congxiaoyao.xber_admin.driverslist.DriverListActivity;
import com.congxiaoyao.xber_admin.driverslist.module.CarDetailParcel;
import com.congxiaoyao.xber_admin.driverslist.taskdetail.TaskDetailActivity;
import com.congxiaoyao.xber_admin.driverslist.widget.CollapsibleHeader;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by guo on 2017/3/29.
 */

public class DriverDetailActivity extends SwipeBackActivity implements CollapsibleHeader {

    private CarDetailParcel parcel;
    private ActivityDriverDetailBinding binding;
    private Drawable shadow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_detail);
        binding.tvCarPlate.setText(getCarDetail().getPlate());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getCarDetail().getUserInfo().getName());

        binding.llContainer.post(new Runnable() {
            @Override
            public void run() {
                int height = binding.toolbar.getHeight();
                binding.llContainer.setTranslationY(height / 2);
            }
        });

        LayerDrawable background = (LayerDrawable) binding.appbar.getBackground();
        shadow = background.findDrawableByLayerId(R.id.drawable_shadow);
        shadow.setAlpha(0);

        binding.tabs.setTabMode(TabLayout.MODE_FIXED);
        binding.viewPager.setAdapter(null);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        binding.tabs.setupWithViewPager(binding.viewPager);
        binding.tabs.setSelectedTabIndicatorColor(ContextCompat
                .getColor(this, R.color.colorWhite));

        binding.imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] fileNames = fileList();
                for (String fileName : fileNames) {
                    if (fileName.startsWith("trace")) {
                        deleteFile(fileName);
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public int getToolbarHeight() {
        return binding.toolbar.getHeight();
    }

    @Override
    public int getTabBarHeight() {
        return binding.tabs.getHeight();
    }

    @Override
    public int getAppbarHeight() {
        return binding.appbar.getHeight();
    }

    @Override
    public View getDriverView() {
        return binding.llContainer;
    }

    @Override
    public Drawable getBackground() {
        return shadow;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;
        private String[] titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[2];
            fragments[0] = new HistoryTaskFragment();
            new HistoryTaskPresenterImpl((HistoryTaskContract.View) fragments[0]);
            fragments[1] = DriverDetailFragment.newInstance(getCarDetail());
            titles = new String[]{"历史记录", "司机详情"};
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    public CarDetailParcel getCarDetail() {
        if (parcel == null) {
            Intent intent = getIntent();
            parcel = intent.getParcelableExtra(DriverListActivity.EXTRA_CARDETIAL);
        }
        return parcel;
    }
}
