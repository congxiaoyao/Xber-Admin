package com.congxiaoyao.xber_admin.publishedtask.bean;

import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

/**
 * Created by congxiaoyao on 2017/4/30.
 */

public interface TaskTrackContact {

    interface View extends LoadableView<Presenter> {

        void showTask(TaskRspAndDriver task);

        void showError();
    }

    interface Presenter extends BasePresenter{

    }
}
