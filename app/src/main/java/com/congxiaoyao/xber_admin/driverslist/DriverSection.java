package com.congxiaoyao.xber_admin.driverslist;

import com.chad.library.adapter.base.entity.SectionEntity;
import com.congxiaoyao.httplib.response.CarDetail;

/**
 * Created by guo on 2017/3/26.
 */

public class DriverSection extends SectionEntity<CarDetail>{

    public DriverSection(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public DriverSection(CarDetail carDetail) {
        super(carDetail);
    }
}
