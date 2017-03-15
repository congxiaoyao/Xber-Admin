package com.congxiaoyao.xber_admin.login;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.mvpbase.SimpleMvpActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.BaseView;

public class LoginActivity extends SimpleMvpActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String getToolbarTitle() {
        return "请登陆";
    }

    @Override
    public BasePresenter getPresenter(BaseView baseView) {
        return new LoginPresenterImpl((LoginContract.View) baseView);
    }

    @Override
    public Fragment getFragment() {
        return new LoginFragment();
    }
}
