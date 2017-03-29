package com.congxiaoyao.xber_admin.driverslist;

import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.presenter.PagedListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;
import com.congxiaoyao.xber_admin.mvpbase.view.PagedListLoadableViewImpl;

/**
 * Created by guo on 2017/3/29.
 */

public interface DriverItemContract {

    interface View extends ListLoadableView<Presenter,TaskRsp>{
        void addExecutingTask(final TaskRsp taskRsp);
    }

    interface Presenter extends PagedListLoadablePresenter{
    }
}
