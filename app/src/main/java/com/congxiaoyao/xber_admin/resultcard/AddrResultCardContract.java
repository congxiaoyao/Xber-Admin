package com.congxiaoyao.xber_admin.resultcard;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

import java.util.List;

import rx.functions.Action0;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public interface AddrResultCardContract {

    interface View extends ListLoadableView<Presenter, Spot> {

        void requestResize(int dataSize);

        void post(Runnable runnable);

        void hideMyself(Action0 callback);

        void setLocationIcon(int id);
    }

    interface Presenter extends ListLoadablePresenter{

        void selectStart();

        void selectEnd();

        void onSelectSpot(Spot spot);
    }

}
