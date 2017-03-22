package com.congxiaoyao.xber_admin.dispatch;

import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

/**
 * Created by guo on 2017/3/22.
 */

public class StratLocationContract  {

    interface View extends ListLoadableView<Presenter,Spot>{
        void clear();
    }

    interface Presenter extends ListLoadablePresenter {

    }
}
