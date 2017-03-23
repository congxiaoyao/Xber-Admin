package com.congxiaoyao.xber_admin.dispatch;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

/**
 * Created by guo on 2017/3/22.
 */

public class ChooseStartLocationFragment extends ListLoadableViewImpl<StratLocationContract.Presenter,Spot>
        implements StratLocationContract.View {

    protected RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int checkedIndex = -1;
    private int lastCheckedInedx = -2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDispatchTaskActivity().hideToolbarButton();
        getDispatchTaskActivity().hideWeekLine();
        View view = inflater.inflate(R.layout.fragment_location_start, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_location);
        Button button = (Button) view.findViewById(R.id.btn_next);
        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPresenter().getType() == 0) {
                    getDispatchTaskActivity().showWeekLine();
                    getDispatchTaskActivity().showToolbarButton();
                }
                getFragmentManager().popBackStack();
                getDispatchTaskActivity().notifyToolBar(ChooseStartLocationFragment.this);
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorPrimary));
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_loading_progress);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(getContext())
                .size(1)
                .margin(DisplayUtils.dp2px(getContext(), 16))
                .colorResId(R.color.colorLightGray)
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedIndex==-1) {
                    Toast.makeText(getContext(), "请选择地点", Toast.LENGTH_SHORT).show();
                    return;
                };
                presenter.setSpot(getAdapter().getData().get(checkedIndex));
                ((DispatchTaskActivity)getContext()).jumpToNext(ChooseStartLocationFragment.this);
            }
        });
        return view;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Spot data) {
        super.convert(viewHolder, data);
        viewHolder.setText(R.id.location, data.getSpotName());
        int position = viewHolder.getLayoutPosition();
        if (position == checkedIndex) {
            viewHolder.setChecked(R.id.location_chcekbox, true);
        } else {
            viewHolder.setChecked(R.id.location_chcekbox, false);
        }
    }

    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_location_start;
    }
    @Override
    public boolean isSupportSwipeRefresh() {
        return true;
    }

    @Override
    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }

    private DispatchTaskActivity getDispatchTaskActivity() {
        return (DispatchTaskActivity) (getContext());
    }

    @Override
    public void clear() {
        checkedIndex = -1;
        lastCheckedInedx = -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG.ME, "onDestroy: ");
    }

    public StratLocationContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void hideSwipeRefreshLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
