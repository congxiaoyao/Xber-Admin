package com.congxiaoyao.xber_admin.driverslist;

import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.SectionEntity;
import com.congxiaoyao.xber_admin.R;

import java.util.List;

/**
 * Created by guo on 2017/3/26.
 */

public class DriverSectionAdapter extends BaseSectionQuickAdapter<DriverSection, BaseViewHolder> {

    public DriverSectionAdapter(int layoutResId, int sectionHeadResId, List<DriverSection> data) {
        super(layoutResId, sectionHeadResId, data);
    }

    @Override
    protected void convertHead(BaseViewHolder helper, DriverSection item) {
        helper.setText(R.id.tv_header, item.header);
    }

    @Override
    protected void convert(BaseViewHolder helper, DriverSection item) {
        helper.setText(R.id.tv_driver_name, item.t.getUserInfo().getName());
        helper.setText(R.id.tv_car_plate, item.t.getPlate());
        helper.setText(R.id.tv_car_type, item.t.getSpec());
    }

}
