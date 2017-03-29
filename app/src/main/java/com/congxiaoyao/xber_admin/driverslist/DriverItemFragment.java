package com.congxiaoyao.xber_admin.driverslist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.driverslist.bean.CarDetailParcel;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.congxiaoyao.xber_admin.mvpbase.view.PagedListLoadableViewImpl;

import java.text.SimpleDateFormat;

/**
 * Created by guo on 2017/3/29.
 */

public class DriverItemFragment extends PagedListLoadableViewImpl<DriverItemContract.Presenter,TaskRsp>
        implements DriverItemContract.View {

    private RecyclerView recyclerView;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drivcer_item, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        super.onCreateView(inflater, container, savedInstanceState);
        CarDetailParcel parcel = ((DriverItemActivity) getContext()).getParcel();
        getAdapter().addHeaderView(getDriverHeader(parcel),0);
        getAdapter().addHeaderView(getHistoryHeader(),1);
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (presenter != null) {
            presenter.subscribe();
        }
        return view;
    }

    public View getDriverHeader(CarDetailParcel parcel) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_header_driver, null);
        ((TextView) view.findViewById(R.id.tv_driver_name)).setText(parcel.getUserInfo().getName());
        ((TextView) view.findViewById(R.id.tv_car_plate)).setText(parcel.getPlate());
        ((TextView) view.findViewById(R.id.tv_car_type)).setText(parcel.getSpec());
        return view;
    }

    public View getHistoryHeader() {
        return LayoutInflater.from(getContext()).inflate(R.layout.item_header_hostry, null);
    }

    @Override
    public boolean isSupportLoadMore() {
        return true;
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return false;
    }

    @Override
    public void scrollToTop() {

    }
    @Override
    protected int getPageSize() {
        return DriverItemPresenterImpl.PAGE_SIZE;
    }

    @Override
    public void addExecutingTask(final TaskRsp taskRsp) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                getAdapter().addHeaderView(getHeaderTask(taskRsp),2);
            }
        });
    }


    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_header_task;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, TaskRsp taskRsp) {
        super.convert(viewHolder, taskRsp);
        viewHolder.setText(R.id.tv_start_spot, taskRsp.getStartSpot().getSpotName());
        viewHolder.setText(R.id.tv_end_spot, taskRsp.getEndSpot().getSpotName());
        viewHolder.setText(R.id.tv_start_time, format.format(taskRsp.getStartTime()));
        viewHolder.setText(R.id.tv_end_time, format.format(taskRsp.getEndTime()));
    }

    public View getHeaderTask(TaskRsp taskRsp) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_header_task, null);
        ((TextView) view.findViewById(R.id.tv_start_spot)).setText(taskRsp.getStartSpot().getSpotName());
        ((TextView) view.findViewById(R.id.tv_end_spot)).setText(taskRsp.getEndSpot().getSpotName());
        ((TextView) view.findViewById(R.id.tv_start_time)).setText(format.format(taskRsp.getStartTime()));
        ((TextView) view.findViewById(R.id.tv_end_time)).setText(format.format(taskRsp.getEndTime()));
        TextView state = (TextView) view.findViewById(R.id.tv_state);
        //我还不信交不上去了？？
        state.setText("运送中");
        state.setTextColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        return view;
    }

    @Override
    public void hideSwipeRefreshLoading() {

    }
}
