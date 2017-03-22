package com.congxiaoyao.xber_admin.resultcard;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/3/22.
 */

public class OnTaskResultCardContract {

    interface View extends LoadableView<Presenter> {

        void hideMySelf(Runnable runnable);
    }

    interface Presenter extends BasePresenter {

        void getCarOnTask(Spot start, Spot end, Action1<List<CarDetail>> callback);

        void destroy(Runnable callback);
    }


}
