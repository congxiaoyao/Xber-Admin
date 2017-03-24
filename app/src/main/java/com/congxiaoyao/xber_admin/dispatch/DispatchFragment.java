package com.congxiaoyao.xber_admin.dispatch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.R;

import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

/**
 * Created by guo on 2017/3/16.
 */

public class DispatchFragment extends ListLoadableViewImpl<DispatchContract.Presenter,CarDetail>
        implements DispatchContract.View  {
    protected RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int checkedIndex = -1;
    private int lastCheckedIndex = -2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_car, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorPrimary));
        Button btn_choose_car_next = (Button) view.findViewById(R.id.btn_next);

        btn_choose_car_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedIndex==-1){
                    Toast.makeText(getContext(), "请选择司机", Toast.LENGTH_LONG).show();
                    return;
                }
                presenter.setCar(getAdapter().getData().get(checkedIndex));
                ((DispatchTaskActivity)getContext()).jumpToNext(DispatchFragment.this);
            }
        });
        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             getFragmentManager().popBackStack();
                ((DispatchTaskActivity) getContext()).notifyToolBar(DispatchFragment.this);
            }
        });
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_progress_bar);
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
                //重复选择认为没做操作
                if (checkedIndex == lastCheckedIndex) return;
                //改变选择的情况
                adapter.notifyItemChanged(checkedIndex);
                if (lastCheckedIndex != -1) adapter.notifyItemChanged(lastCheckedIndex);

                lastCheckedIndex = checkedIndex;
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
    protected void convert(BaseViewHolder viewHolder, CarDetail data) {
        super.convert(viewHolder, data);
        viewHolder.setText(R.id.tv_driver_name, data.getUserInfo().getName());
        viewHolder.setText(R.id.tv_car_plate, data.getPlate());
        viewHolder.setText(R.id.tv_car_type, data.getSpec());
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
        lastCheckedIndex = -1;
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                getAdapter().notifyDataSetChanged();
            }
        });
    }

    @Override
    public DispatchTaskActivity getParent() {
        return (DispatchTaskActivity) getContext();
    }

    @Override
    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }
}
