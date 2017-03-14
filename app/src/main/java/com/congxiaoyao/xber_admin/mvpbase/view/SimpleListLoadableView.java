package com.congxiaoyao.xber_admin.mvpbase.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenter;

/**
 * {@link ListLoadableViewImpl}过于抽象不易使用，所以此类就是为了解决他的使用问题
 * 为了简化使用 这里直接设置fragment的布局文件为R.layout.fragment_swipe_list 同时完成了他们的初始化操作
 * 于是这里默认开启了下拉加载更多和底部加载更多并实现了相关方法 适用于那些可下拉刷新的列表场景
 *
 * Created by congxiaoyao on 2016/8/27.
 */
public class SimpleListLoadableView<T extends ListLoadablePresenter, D> extends ListLoadableViewImpl<T, D> {

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_swipe_list, container, false);
//        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
//                R.color.colorLightGreen));
//        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
//        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_loading_progress);
//        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
//                .Builder(getContext())
//                .size(1)
//                .margin(DisplayUtils.dp2px(getContext(), 16), 0)
//                .visibilityProvider((position, parent) ->
//                        position == (getAdapter().getData().size() - 1))
//                .colorResId(R.color.colorLightGray)
//                .build());
//        super.onCreateView(inflater, container, savedInstanceState);
//        recyclerView.setAdapter(getAdapter());
//        getAdapter().setOnRecyclerViewItemClickListener((item, position) ->
//                onItemClicked(getAdapter().getItem(position), item));
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        if (presenter != null) presenter.subscribe();
        //TODO
        return new Button(container.getContext());
    }

    @Override
    public boolean isSupportLoadMore() {
        return true;
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return true;
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @Override
    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void hideSwipeRefreshLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }

    protected void onItemClicked(D d, View view) {

    }
}