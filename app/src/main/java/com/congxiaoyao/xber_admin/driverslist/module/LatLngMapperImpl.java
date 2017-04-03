package com.congxiaoyao.xber_admin.driverslist.module;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by congxiaoyao on 2017/4/2.
 */

public class LatLngMapperImpl implements LatLngMapper<LatLng> {
    @Override
    public double getLat(LatLng latLng) {
        return 0;
    }

    @Override
    public double getLng(LatLng latLng) {
        return 0;
    }

    @Override
    public LatLng toObject(double lat, double lng) {
        return new LatLng(lat, lng);
    }
}
