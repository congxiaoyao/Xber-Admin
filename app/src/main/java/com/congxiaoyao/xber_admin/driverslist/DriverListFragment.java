package com.congxiaoyao.xber_admin.driverslist;

import android.content.Intent;
import android.graphics.Canvas;
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
import android.widget.QuickContactBadge;

import com.bigkoo.quicksidebar.QuickSideBarTipsView;
import com.bigkoo.quicksidebar.QuickSideBarView;
import com.bigkoo.quicksidebar.listener.OnQuickSideBarTouchListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.httplib.response.BasicUserInfo;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.driverslist.bean.BasicUserInfoParcel;
import com.congxiaoyao.xber_admin.driverslist.bean.CarDetailParcel;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

/**
 * Created by guo on 2017/3/26.
 */

public class DriverListFragment extends ListLoadableViewImpl<DriverListContract.Presenter,DriverSection>
        implements DriverListContract.View ,OnQuickSideBarTouchListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private QuickSideBarTipsView quickSideBarTipsView;
    private QuickSideBarView quickSideBarView;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.carList);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorPrimary));
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_progress_bar);
        super.onCreateView(inflater, container, savedInstanceState);
        DriverSectionAdapter adapter = new DriverSectionAdapter(R.layout.item_car_driver_list,
                R.layout.item_header_car, getData());
        bindAdapterAndDataSet(adapter, getData());
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(getContext())
                .size(1)
                .colorResId(R.color.colorLightGray)
                .build());
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(getContext(), DriverItemActivity.class);
                CarDetail t = getData().get(position).t;
                CarDetailParcel parcel = getParcel(t);
                intent.putExtra(DriverListActivity.EXTRA_CARDETIAL, parcel);
                startActivity(intent);
            }
        });
        quickSideBarView = (QuickSideBarView) view.findViewById(R.id.quickSideBarView);
        quickSideBarTipsView = (QuickSideBarTipsView) view.findViewById(R.id.quickSideBarTips);
        quickSideBarView.setOnQuickSideBarTouchListener(this);
        if (presenter != null) {
            presenter.subscribe();
        }
        return view;
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
    public void scrollToTop() {

    }

    public CarDetailParcel getParcel(CarDetail t) {
        CarDetailParcel parcel = new CarDetailParcel();
        BasicUserInfoParcel basicUserInfoParcel = new BasicUserInfoParcel();
        BasicUserInfo userInfo = t.getUserInfo();
        basicUserInfoParcel.setUserId(userInfo.getUserId());
        basicUserInfoParcel.setAge(userInfo.getAge());
        basicUserInfoParcel.setAvatar(userInfo.getAvatar());
        basicUserInfoParcel.setGender(userInfo.getGender());
        basicUserInfoParcel.setName(userInfo.getName());
        parcel.setCarId(t.getCarId());
        parcel.setPlate(t.getPlate());
        parcel.setSpec(t.getSpec());
        parcel.setUserInfo(basicUserInfoParcel);
        return parcel;
    }

    @Override
    public void hideSwipeRefreshLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLetterChanged(String letter, int position, float y) {
        quickSideBarTipsView.setText(letter, position, y);
        recyclerView.smoothScrollToPosition(presenter.getIndexByChar(letter.charAt(0)));
    }

    @Override
    public void onLetterTouching(boolean touching) {
        quickSideBarTipsView.setVisibility(touching ? View.VISIBLE : View.INVISIBLE);
    }
}
