package com.congxiaoyao.xber_admin.dispatch;

import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

/**
 * Created by guo on 2017/3/16.
 */

public interface DispatchContract{

    public interface View extends ListLoadableView<Presenter,CheckedFreeCar> {

        void clear();
    };

    public interface Presenter extends ListLoadablePresenter {
        void setCarId(long carId);
    };



}
