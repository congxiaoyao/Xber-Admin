package com.congxiaoyao.xber_admin.dispatch;

import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

/**
 * Created by guo on 2017/3/23.
 */

public interface DistributeContract {

    interface View extends LoadableView<Presenter>{

    }

    interface Presenter extends BasePresenter{

    }
}
