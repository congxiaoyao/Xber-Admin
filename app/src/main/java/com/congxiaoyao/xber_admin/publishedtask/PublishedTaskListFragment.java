package com.congxiaoyao.xber_admin.publishedtask;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.mvpbase.view.PagedListLoadableViewImpl;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskAndDriver;

import java.text.SimpleDateFormat;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class PublishedTaskListFragment
        extends PagedListLoadableViewImpl<PublishedTaskContract.Presenter,TaskAndDriver>
        implements PublishedTaskContract.View{

    public static final String KEY_TASK = "KEY_TASK";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_published_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_progress_bar);
        super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.ll_container:
                        Intent intent = new Intent(getContext(), TaskTrackActivity.class);
                        TaskAndDriver taskAndDriver = getData().get(position);
                        intent.putExtra(KEY_TASK, taskAndDriver);
                        startActivity(intent);
                        break;
                }
            }
        });
        if (presenter != null) presenter.subscribe();
        return view;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_published_task;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, TaskAndDriver data) {
        viewHolder.setText(R.id.tv_driver_name, "    "+data.getCarDetail().getUserInfo().getName())
                .setText(R.id.tv_start_spot, data.getStartSpot().getSpotName())
                .setText(R.id.tv_end_spot, data.getEndSpot().getSpotName())
                .setText(R.id.tv_transport_content, data.getContent())
                .setText(R.id.tv_start_time, format.format(data.getStartTime()));
        viewHolder.addOnClickListener(R.id.ll_container);
    }

    @Override
    public boolean isSupportLoadMore() {
        return true;
    }

    @Override
    protected int getPageSize() {
        return PublishedTaskListPresenter.PAGE_SIZE;
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return false;
    }

    @Override
    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    protected boolean isSupportToolbarDoubleClick() {
        return true;
    }

    @Override
    public void hideSwipeRefreshLoading() {

    }
}
