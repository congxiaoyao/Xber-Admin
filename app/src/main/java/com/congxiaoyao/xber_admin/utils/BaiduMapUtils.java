package com.congxiaoyao.xber_admin.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.location.utils.Line;
import com.congxiaoyao.xber_admin.TAG;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class BaiduMapUtils {

    private static Point screenCenter = null;
    private static Point screenSize = null;

    private static final Point ZERO = new Point();
    private static final Point temp = new Point();

    public static LatLng getScreenCenterLatLng(Context context, BaiduMap baiduMap) {
        getScreenCenterPoint((Activity) context);
        return baiduMap.getProjection().fromScreenLocation(screenCenter);
    }

    private static void getScreenCenterPoint(Activity context) {
        if (screenCenter == null) {
            screenCenter = new Point();
            context.getWindowManager().getDefaultDisplay().getSize(screenCenter);
            screenCenter.x = screenCenter.x / 2;
            screenCenter.y = screenCenter.y / 2;
        }
    }

    public static double getScreenRadius(Context context, BaiduMap baiduMap) {
        getScreenSize((Activity) context);
        Projection projection = baiduMap.getProjection();
        LatLng lt = projection.fromScreenLocation(ZERO);
        LatLng rb = projection.fromScreenLocation(screenSize);
        Line line = new Line(lt.longitude, lt.latitude, rb.longitude, rb.latitude);
        return line.getLength() / 2;
    }

    private static void getScreenSize(Activity context) {
        if (screenSize == null) {
            screenSize = new Point();
            context.getWindowManager().getDefaultDisplay().getSize(screenSize);
        }
    }

    /**
     * latlng是否在屏幕内
     * @param context
     * @param baiduMap
     * @param latLng
     * @return
     */
    public static boolean isInScreen(Context context, BaiduMap baiduMap, LatLng latLng) {
        getScreenSize((Activity) context);
        Projection projection = baiduMap.getProjection();
        Point point = projection.toScreenLocation(latLng);
        return point.x > 0 && point.x < screenSize.x && point.y > 0 && point.y < screenSize.y;
    }

    /**
     * 当前地图的缩放等级
     * @param baiduMap
     * @return
     */
    public static float getZoomLevel(BaiduMap baiduMap) {
        return baiduMap.getMapStatus().zoom;
    }

    public static void moveToBounds(BaiduMap map, LatLngBounds bounds, int width, int height) {
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngBounds(bounds,
                width, height);
        map.setMapStatus(update);
    }

    public static void moveToBoundsAnimate(BaiduMap map, LatLngBounds bounds, int width, int height) {
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngBounds(bounds,
                width, height);
        map.animateMapStatus(update);
    }

    public static void moveToLatLng(BaiduMap map, double lat, double lng, int zoom, boolean animate) {
        if (animate) {
            moveToLatLng(map, lat, lng, zoom);
            return;
        }
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(new LatLng(lat, lng))
                .zoom(zoom).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.setMapStatus(mMapStatusUpdate);
    }

    public static void moveToLatLng(BaiduMap map, double lat, double lng, int zoom) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(new LatLng(lat, lng))
                .zoom(zoom)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void moveToLatLng(BaiduMap map, double lat, double lng) {
        moveToLatLng(map, lat, lng, 16);
    }

    public static void moveToPoint(BaiduMap map, Point point) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .targetScreen(point).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void moveToPoint(BaiduMap map, int x, int y) {
        temp.x = x;
        temp.y = y;
        moveToPoint(map, temp);
    }

    public static void zoom(BaiduMap map, float level) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .zoom(level).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void zoomToDefault(BaiduMap map) {
        zoom(map, 16);
    }
}
