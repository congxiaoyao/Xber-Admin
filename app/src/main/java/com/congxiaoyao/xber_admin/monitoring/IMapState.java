package com.congxiaoyao.xber_admin.monitoring;

import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public interface IMapState {

    void onMapStatusChangeFinish(MapStatus mapStatus);

    boolean onMarkerClick(Marker marker);

    void onMapClick(LatLng latLng);
}
