package com.congxiaoyao.xber_admin.mvpbase.view;

import android.content.Context;

import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;

/**
 * Created by congxiaoyao on 2016/8/16.
 */
public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);

    Context getContext();
}
