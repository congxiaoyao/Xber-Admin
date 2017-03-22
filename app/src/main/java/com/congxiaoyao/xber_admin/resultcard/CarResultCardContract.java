package com.congxiaoyao.xber_admin.resultcard;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

import rx.functions.Action0;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public interface CarResultCardContract {

    interface View extends ListLoadableView<Presenter, CarDetail> {

        void requestResize(int dataSize);

        void post(Runnable runnable);

        void hideMyself(Action0 callback);
    }

    interface Presenter extends ListLoadablePresenter{

        void setOnCarSelectedListener(OnCarSelectedListener listener);

        void search(String content);

        void callClick(CarDetail carDetail);
    }

    interface OnCarSelectedListener {

        void onCarSelected(CarDetail carDetail);
    }
}
