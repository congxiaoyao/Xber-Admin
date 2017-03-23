package com.congxiaoyao.xber_admin.dispatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.congxiaoyao.httplib.request.body.LaunchTaskRequest;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableViewImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by guo on 2017/3/22.
 */

public class DistributeFragment extends LoadableViewImpl<DistributeContract.Presenter>
        implements DistributeContract.View{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_distribute, container, false);
        final DispatchTaskActivity context = (DispatchTaskActivity) getContext();
        LaunchTaskRequest request = context.getData();
        CarDetail carDetail = context.getCar();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
        ((TextView) view.findViewById(R.id.tv_start_date)).setText(format1.format(new Date(request.getStartTime())));
        ((TextView) view.findViewById(R.id.tv_start_time)).setText(format2.format(new Date(request.getStartTime())));
        ((TextView) view.findViewById(R.id.tv_end_date)).setText(format1.format(new Date(request.getEndTime())));
        ((TextView) view.findViewById(R.id.tv_end_time)).setText(format2.format(new Date(request.getEndTime())));
        ((TextView) view.findViewById(R.id.tv_car_user)).setText(carDetail.getUserInfo().getName());
        ((TextView) view.findViewById(R.id.tv_car_type)).setText(carDetail.getSpec());
        ((TextView) view.findViewById(R.id.tv_car_plate)).setText(carDetail.getPlate());
        ((TextView) view.findViewById(R.id.tv_start_location)).setText(context.getStart_spot().getSpotName());
        ((TextView) view.findViewById(R.id.tv_end_location)).setText(context.getEnd_spot().getSpotName());
        ((TextView) view.findViewById(R.id.tv_remark)).setText(request.getContent());
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_progress_bar);
        view.findViewById(R.id.btn_distribute_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                context.notifyToolBar(DistributeFragment.this);
            }
        });
        view.findViewById(R.id.btn_distribute_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter != null) {
                    presenter.subscribe();
                }
            }
        });
        return view;
    }

}
