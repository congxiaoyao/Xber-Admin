package com.congxiaoyao.xber_admin.monitoring.carinfo;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

/**
 * Created by congxiaoyao on 2017/4/25.
 */

public interface CarInfoContract {

    interface View extends LoadableView<Presenter> {

        void showContentView();

        void hideContentView();

        void bindData(CarDetail carDetail);
    }

    interface Presenter extends BasePresenter {

        void onClick(android.view.View view);

        void onShowLocation(android.view.View view);
    }



}
