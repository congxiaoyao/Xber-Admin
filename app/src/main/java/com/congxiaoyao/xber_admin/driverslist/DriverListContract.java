package com.congxiaoyao.xber_admin.driverslist;

import com.congxiaoyao.xber_admin.driverslist.module.DriverSection;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

/**
 * Created by guo on 2017/3/26.
 */

public interface DriverListContract {

    interface View extends ListLoadableView<Presenter,DriverSection> {

    }

    interface Presenter extends ListLoadablePresenter{

        int getIndexByChar(char x);
    }

}
