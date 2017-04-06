package com.congxiaoyao.xber_admin.publishedtask;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.xber_admin.Admin;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ActivityPublishedTaskBinding;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class PublishedTaskActivity extends SwipeBackActivity {

    private ActivityPublishedTaskBinding binding;
    private Admin admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        admin = Admin.fromSharedPreference(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_published_task);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("我的派发记录");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.tabs.setTabMode(TabLayout.MODE_FIXED);
        binding.viewPager.setOffscreenPageLimit(5);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        binding.tabs.setupWithViewPager(binding.viewPager);
        binding.tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorWhite));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private PublishedTaskListFragment[] fragments;
        private String[] titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            titles = new String[]{"已送达", "待出发", "运输中"};
            int[] requsetTypes = {Task.STATUS_COMPLETED, Task.STATUS_EXECUTING, Task.STATUS_DELIVERED};
            fragments = new PublishedTaskListFragment[titles.length];
            for (int i = 0; i < titles.length; i++) {
                fragments[i] = new PublishedTaskListFragment();
                new PublishedTaskListPresenter(fragments[i])
                        .setUserId(admin.getUserId())
                        .setRequestType(requsetTypes[i]);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
