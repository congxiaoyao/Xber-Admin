package com.congxiaoyao.xber_admin.driverslist;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.dispatch.DispatchContract;
import com.congxiaoyao.xber_admin.mvpbase.SimpleMvpActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.BaseView;

public class DriverListActivity extends SimpleMvpActivity {

    public static final String EXTRA_CARDETIAL = "carDetial";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String getToolbarTitle() {
        return "司机列表";
    }

    @Override
    public BasePresenter getPresenter(BaseView baseView) {
        return new DriverListPresenterImpl(((DriverListContract.View) baseView));
    }

    @Override
    public Fragment getFragment() {
        return new DriverListFragment();
    }
}
