package com.congxiaoyao.xber_admin.login;

import com.congxiaoyao.xber_admin.Admin;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public interface LoginContract {

    interface View extends LoadableView<Presenter> {

        void showLoginError(String msg);

        void showLoginSuccess();
    }

    interface Presenter extends BasePresenter {

        Admin getAdmin();

        void login(String userName, String password);

        void setLoginResult(int tag);
    }
}
