package com.congxiaoyao.xber_admin.driverslist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;

import com.congxiaoyao.xber_admin.driverslist.bean.CarDetailParcel;
import com.congxiaoyao.xber_admin.mvpbase.SimpleMvpActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.BaseView;

/**
 * Created by guo on 2017/3/29.
 */

public class DriverItemActivity extends SimpleMvpActivity{

    private CarDetailParcel parcel;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    public CarDetailParcel getParcel() {
        if (parcel==null) {
            Intent intent = getIntent();
            parcel = intent.getParcelableExtra(DriverListActivity.EXTRA_CARDETIAL);
        }
        return parcel;
    }

    @Override
    public String getToolbarTitle() {
        return "司机详情";
    }

    @Override
    public BasePresenter getPresenter(BaseView baseView) {
        return new  DriverItemPresenterImpl((DriverItemContract.View) baseView);
    }

    @Override
    public Fragment getFragment() {
        return new DriverItemFragment()
                ;
    }
}
