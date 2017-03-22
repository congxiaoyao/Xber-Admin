package com.congxiaoyao.xber_admin.dispatch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.congxiaoyao.xber_admin.R;

/**
 * Created by guo on 2017/3/22.
 */

public class DistributeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_distribute, container, false);
        TextView tv_start_date = (TextView) view.findViewById(R.id.tv_start_date);
        TextView tv_end_date = (TextView) view.findViewById(R.id.tv_end_date);
        TextView tv_start_time = (TextView) view.findViewById(R.id.tv_start_time);
        TextView tv_end_time = (TextView) view.findViewById(R.id.tv_end_time);
        TextView tv_car_plate = (TextView) view.findViewById(R.id.tv_car_plate);
        TextView tv_car_type = (TextView) view.findViewById(R.id.tv_car_type);
        TextView tv_car_user = (TextView) view.findViewById(R.id.tv_car_user);
        TextView tv_start_location = (TextView) view.findViewById(R.id.tv_start_location);
        TextView tv_end_location= (TextView) view.findViewById(R.id.tv_end_location);
        return view;
    }

}
