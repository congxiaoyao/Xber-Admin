package com.congxiaoyao.xber_admin.dispatch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.xber_admin.R;

import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

/**
 * Created by guo on 2017/3/16.
 */

public class DispatchFragment extends ListLoadableViewImpl<DispatchContract.Presenter,CheckedFreeCar>
        implements DispatchContract.View  {
    protected RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int checkedIndex = -1;
    private int lastCheckedInedx = -2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_car, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorPrimary));

        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_loading_progress);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(getContext())
                .size(1)
                .margin(DisplayUtils.dp2px(getContext(),16),0)
                .colorResId(R.color.colorDarkGray)
                .build());
        super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(null);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                checkedIndex = position;
                if (checkedIndex == lastCheckedInedx) {
                    checkedIndex = -1;
                }
                if (checkedIndex != -1) {
                    adapter.notifyItemChanged(checkedIndex);
                }
                if (lastCheckedInedx != -1) {
                    adapter.notifyItemChanged(lastCheckedInedx);
                }
                lastCheckedInedx = checkedIndex;
            }
        });
        if (presenter != null) {
            presenter.subscribe();
        }
        return view;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_choose_car;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, CheckedFreeCar data) {
        super.convert(viewHolder, data);
        viewHolder.setText(R.id.tv_driver_name, data.getDriverName());
        viewHolder.setText(R.id.tv_car_plate, data.getCarPlate());
        viewHolder.setText(R.id.tv_car_type, data.getCarType());
        int position = viewHolder.getLayoutPosition();
        if (position == checkedIndex) {
            viewHolder.setChecked(R.id.checkbox, true);
        }else {
            viewHolder.setChecked(R.id.checkbox, false);
        }
    }

    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return true;
    }

    @Override
    public void hideSwipeRefreshLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }
    @Override
    public void clear() {
        checkedIndex = -1;
        lastCheckedInedx = -1;
    }

    @Override
    public void scrollToTop() {
    }
}