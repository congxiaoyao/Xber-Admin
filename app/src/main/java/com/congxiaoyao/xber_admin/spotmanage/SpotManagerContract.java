package com.congxiaoyao.xber_admin.spotmanage;

import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

/**
 * Created by guo on 2017/3/24.
 */

public interface SpotManagerContract {

    interface View extends ListLoadableView<Presenter,Spot> {

    }

    interface Presenter extends ListLoadablePresenter{

        void remove(Spot spot);
    }

}
