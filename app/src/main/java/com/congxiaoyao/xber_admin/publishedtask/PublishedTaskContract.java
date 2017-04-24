package com.congxiaoyao.xber_admin.publishedtask;

import com.congxiaoyao.xber_admin.mvpbase.presenter.PagedListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskRspAndDriver;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public interface PublishedTaskContract {

    interface View extends ListLoadableView<Presenter, TaskRspAndDriver> {

    }

    interface Presenter extends PagedListLoadablePresenter {

    }
}
