package com.congxiaoyao.xber_admin.mvpbase.view;


import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;

/**
 * Created by congxiaoyao on 2016/8/25.
 */
public interface LoadableView<T extends BasePresenter> extends BaseView<T> {

    void showLoading();

    void hideLoading();
}
