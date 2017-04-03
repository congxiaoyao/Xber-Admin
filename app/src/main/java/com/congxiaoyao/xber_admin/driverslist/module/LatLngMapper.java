package com.congxiaoyao.xber_admin.driverslist.module;

/**
 * Created by congxiaoyao on 2017/4/2.
 */
public interface LatLngMapper<T> {

    double getLat(T t);

    double getLng(T t);

    T toObject(double lat, double lng);
}
