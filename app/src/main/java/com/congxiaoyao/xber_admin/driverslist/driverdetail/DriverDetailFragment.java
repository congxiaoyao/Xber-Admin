package com.congxiaoyao.xber_admin.driverslist.driverdetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.FragmentDriverDetailBinding;
import com.congxiaoyao.xber_admin.driverslist.module.CarDetailParcel;

public class DriverDetailFragment extends Fragment {

    private static final String KEY_DATA = "KEY_DATA";

    private CarDetailParcel carDetail;
    private FragmentDriverDetailBinding binding;

    public DriverDetailFragment() {
    }

    public static DriverDetailFragment newInstance(CarDetailParcel carDetail) {
        DriverDetailFragment fragment = new DriverDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_DATA, carDetail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            carDetail = getArguments().getParcelable(KEY_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_detail, container, false);
        binding.setData(carDetail);
        return binding.getRoot();
    }
}
