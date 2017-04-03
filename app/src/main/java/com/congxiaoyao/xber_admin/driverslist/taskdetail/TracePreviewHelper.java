package com.congxiaoyao.xber_admin.driverslist.taskdetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.location.utils.Ray;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.XberApplication;
import com.congxiaoyao.xber_admin.utils.MathUtils;
import com.congxiaoyao.xber_admin.utils.ParcelableUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by congxiaoyao on 2017/4/1.
 */

public class TracePreviewHelper implements TraceContract.View ,BaiduMap.SnapshotReadyCallback {

    private final View progressBar;
    private final ImageView imageView;
    private BaiduMap baiduMap;
    private FrameLayout mapContainer;
    private TraceContract.Presenter presenter;
    private TextureMapView mapView;

    private boolean isLoaded = false;
    private LatLngBounds latlngBounds;

    public TracePreviewHelper(View progressBar, ImageView imageView,
                              FrameLayout mapContainer) {
        this.progressBar = progressBar;
        this.imageView = imageView;
        this.mapContainer = mapContainer;
    }

    private void showTraceThreadSafe(List<LatLng> list) {
        if (baiduMap == null) return;
        Log.d(TAG.ME, "showTraceThreadSafe: " + Thread.currentThread().getName());
        //抽稀
        list = verticalIntervalLimitFilter(list);
        //绘制折线
        OverlayOptions ooPolyline = new PolylineOptions().width(8)
                .color(getContext().getColor(R.color.colorPrimary))
                .points(list);
        baiduMap.addOverlay(ooPolyline);
        //绘制点
        int len = list.size() < 15 ? list.size() : 15;
        List<OverlayOptions> optionList = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            int index = (int) MathUtils.map(0, len - 1, 0, list.size() - 1, i);
            optionList.add(new DotOptions().center(list.get(index))
                    .color(getContext().getColor(R.color.colorPrimaryDark))
                    .radius(12));
        }
        baiduMap.addOverlays(optionList);

        //起止标注
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_pin))
                .position(list.get(0));
        baiduMap.addOverlay(markerOptions);

        markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_pin_end2))
                .position(list.get(list.size() - 1));
        baiduMap.addOverlay(markerOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : list) {
            builder.include(latLng);
        }
        latlngBounds = builder.build();
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngBounds(latlngBounds,
                progressBar.getWidth(), progressBar.getWidth());
        baiduMap.setMapStatus(update);

        saveLatlngBoundsToFile();
    }

    /**
     * 保存 latlngBounds 到文件
     */
    private void saveLatlngBoundsToFile() {
        byte[] marshall = ParcelableUtil.marshall(latlngBounds);
        FileOutputStream outputStream = null;
        try {
            outputStream = getContext()
                    .openFileOutput(presenter.getBoundsFileName(), MODE_PRIVATE);
            outputStream.write(marshall);
            outputStream.flush();
        } catch (IOException e) {
            Log.d(TAG.ME, "save bounds: ", e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.d(TAG.ME, "save bounds: ", e);
            }
        }
    }

    private void createMapViewAndShowTrace(final List<LatLng> list) {
        Log.d(TAG.ME, "createMapViewAndShowTrace: ");
        mapView = new TextureMapView(getContext());
        mapView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mapContainer.addView(mapView);
        baiduMap = mapView.getMap();
        baiduMap.setOnMapRenderCallbadk(new BaiduMap.OnMapRenderCallback() {
            @Override
            public void onMapRenderFinished() {
                int height = mapView.getHeight();
                int width = mapView.getWidth();
                Rect rect = new Rect(0, (height - width) / 2, width, 0);
                rect.bottom = rect.top + width;
                baiduMap.snapshotScope(rect, TracePreviewHelper.this);
                baiduMap.setOnMapRenderCallbadk(null);
            }
        });
        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showTraceThreadSafe(list);
                    }
                }).start();
            }
        });
    }

    @Override
    public void onSnapshotReady(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                onSnapshotReadyThreadSafe(bitmap);
            }
        }).start();
    }

    public void onSnapshotReadyThreadSafe(Bitmap bitmap) {
        Log.d(TAG.ME, "save bitmap: " + Thread.currentThread().getName());
        if (bitmap == null) {
            showError();
            return;
        }
        try {
            boolean compress = bitmap.compress(Bitmap.CompressFormat.WEBP, 100,
                    getContext().openFileOutput(presenter.getImageFileName(), MODE_PRIVATE));
            bitmap.recycle();
            if (compress) {
                showBitmap();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void showBitmap() {
        Log.d(TAG.ME, "showBitmap: ");
        if (presenter == null || imageView == null || isLoaded) return;
        Observable.just(presenter.getTaskId()).map(new Func1<Long, Bitmap>() {
            @Override
            public Bitmap call(Long taskId) {
                try {
                    Log.d(TAG.ME, "decode bitmap: "+Thread.currentThread().getName());
                    return BitmapFactory.decodeStream(getContext()
                            .openFileInput(presenter.getImageFileName()));
                } catch (FileNotFoundException e) {
                    if (!isLoaded) {
                        Log.d(TAG.ME, "subscribe: ");
                        presenter.subscribe();
                    }
                }
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                if (bitmap == null) return;
                imageView.setImageBitmap(bitmap);
                ((XberApplication) getContext().getApplicationContext()).cachePreviewBitmap(bitmap);
                isLoaded = true;
                hideLoading();
                if (mapView != null) {
                    mapContainer.removeAllViews();
                    mapView.onDestroy();
                    mapView = null;
                }
            }
        });
    }

    /**
     * 垂距限值法抽稀 原理见百度百科
     *
     * @param list
     * @return
     */
    private List<LatLng> verticalIntervalLimitFilter(List<LatLng> list) {
        if (list == null || list.size() < 3) return list;
        List<LatLng> result = new ArrayList<>(list.size() / 10);
        final double t = Math.pow(0.00005, 2);
        int current = 1, end = 2, size = list.size();
        LatLng p0 = list.get(0), pm = list.get(1), p1 = list.get(2);
        Ray ray = new Ray(p0.longitude, p0.latitude, p1.longitude, p1.latitude);
        //第一个点一定要
        result.add(p0);
        while (end < size - 1) {
            setRayP1(ray, p1);
            double d = distanceSqOfLatLng(ray, pm);
            //距离小于阈值 跳过这个点
            if (d < t) {
                pm = list.get(++current);
                p1 = list.get(++end);
            }
            //距离大于阈值 要保留这个点
            else {
                result.add(pm);
                setRayP1(ray, pm);
                ray.changeInitalPoint();
                pm = list.get(current = current + 1);
                p1 = list.get(end = current + 1);
            }
        }
        //最后一个点也要
        result.add(list.get(list.size() - 1));
        Log.d(TAG.ME, "verticalIntervalLimitFilter: result.len = " + result.size());
        return result;
    }

    private static double distanceSqOfLatLng(Ray ray, LatLng latLng) {
        return ray.distanceSqOfPoint(latLng.longitude, latLng.latitude);
    }

    public static void setRayP1(Ray ray, LatLng latLng) {
        ray.setP1(latLng.longitude, latLng.latitude);
    }

    @Override
    public void showTrace(final List<LatLng> list) {
        createMapViewAndShowTrace(list);
    }

    @Override
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError() {
        ((ViewGroup) progressBar.getParent()).findViewById(R.id.tv_error_hint).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.trace_preview2);
        ViewGroup parent = (ViewGroup) this.progressBar;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ProgressBar) {
                child.setVisibility(View.GONE);
                return;
            }
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public LatLngBounds getLatlngBounds() {
        return latlngBounds;
    }

    @Override
    public void setPresenter(TraceContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return progressBar.getContext();
    }

    public TextureMapView getMapView() {
        return mapView;
    }
}
