package com.congxiaoyao.xber_admin.mvpbase.view;


import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;

import java.util.List;

/**
 * Created by congxiaoyao on 2016/8/26.
 */
public interface ListLoadableView<T extends BasePresenter, D> extends LoadableView<T>{

    /**
     * 当没有数据时显示的内容
     */
    void showDataEmpty();

    /**
     * 当网络出问题的时候显示的内容
     */
    void showNetworkError();

    /**
     * 添加数据并显示
     * @param data
     */
    void addData(List<D> data);

    /**
     * 清掉所有的view包括列表 变成一个什么都不显示的东西
     */
    void showNothing();

    /**
     * 滚动recyclerView到顶部
     */
    void scrollToTop();

    /**
     * 隐藏下拉刷新的小圆圈
     */
    void hideSwipeRefreshLoading();

}