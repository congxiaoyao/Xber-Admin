package com.congxiaoyao.xber_admin.mvpbase.presenter;

import com.congxiaoyao.httplib.response.Page;

import java.util.Date;

/**
 * Created by congxiaoyao on 2016/8/26.
 */
public interface ListLoadablePresenter extends BasePresenter {

    void savePage(Date latestDate, Page page);

    boolean hasMoreData();

    void loadMoreData();

    void refreshData();
}
