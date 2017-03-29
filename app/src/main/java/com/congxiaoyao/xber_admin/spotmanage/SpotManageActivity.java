package com.congxiaoyao.xber_admin.spotmanage;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.mvpbase.SimpleMvpActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.BaseView;

public class SpotManageActivity extends SimpleMvpActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String getToolbarTitle() {
        return "任务点管理";
    }

    @Override
    public BasePresenter getPresenter(BaseView baseView) {
        return new SpotManagerPresenterImpl((SpotManagerContract.View) baseView);
    }
    @Override
    public Fragment getFragment() {
        return new SpotManagerFragment();
    }
}
