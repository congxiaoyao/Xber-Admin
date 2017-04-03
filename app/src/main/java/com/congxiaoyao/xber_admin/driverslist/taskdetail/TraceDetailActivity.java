package com.congxiaoyao.xber_admin.driverslist.taskdetail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.XberApplication;
import com.congxiaoyao.xber_admin.databinding.ActivityTraceDetailBinding;
import com.congxiaoyao.xber_admin.helpers.MapActivityHelper;
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;
import com.congxiaoyao.xber_admin.utils.MathUtils;
import com.congxiaoyao.xber_admin.utils.ParcelableUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import rx.Observable;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;

public class TraceDetailActivity extends AppCompatActivity
implements TraceContract.View{

    private TraceContract.Presenter presenter;

    private ActivityTraceDetailBinding binding;
    private BaiduMap baiduMap;
    private TextureMapView mapView;
    private Marker start;
    private Marker end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取传递过来的taskId
        long taskId = getIntent().getLongExtra(TaskDetailActivity.KEY_TASK_ID, -1);
        if (taskId == -1) {
            showError();
            return;
        }

        //注入Presenter
        new TracePresenter(this, taskId);

        //获取bitmap
        Bitmap bitmap = ((XberApplication) getApplication()).getCachedBitmap();
        if (bitmap == null) {
            showError();
            return;
        }

        //初始化latlngBounds
        final LatLngBounds bounds = readLatLngBounds(presenter.getBoundsFileName());
        if (bounds == null) {
            showError();
            return;
        }

        //初始化views 并请求数据
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trace_detail);
        MapActivityHelper.showStatusBar(binding.statusBar);
        binding.imgTracePreview.setImageBitmap(bitmap);
        mapView = binding.mapView;
        baiduMap = mapView.getMap();
        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                int width = mapView.getWidth();
                BaiduMapUtils.moveToBounds(baiduMap, bounds, width, width);
                presenter.subscribe();
            }
        });
        baiduMap.setOnMapRenderCallbadk(new BaiduMap.OnMapRenderCallback() {
            @Override
            public void onMapRenderFinished() {
                hideLoading();
                baiduMap.setOnMapLoadedCallback(null);
            }
        });
    }

    public LatLngBounds readLatLngBounds(String fileName) {
        try {
            FileInputStream stream = openFileInput(fileName);
            byte[] bytes = TracePresenter.toByteArray(stream.getChannel());
            return ParcelableUtil.unmarshall(bytes, LatLngBounds.CREATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void showTrace(List<LatLng> list) {
        //绘制折线
        OverlayOptions ooPolyline = new PolylineOptions().width(8)
                .color(getContext().getColor(R.color.colorPrimary))
                .points(list);
        baiduMap.addOverlay(ooPolyline);

        //起止标注
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_pin))
                .position(list.get(0))
                .alpha(0);
        start = (Marker) baiduMap.addOverlay(markerOptions);

        markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.xber_pin_end2))
                .position(list.get(list.size() - 1))
                .alpha(0);
        end = (Marker) baiduMap.addOverlay(markerOptions);
    }

    @Override
    public void showError() {
        Snackbar.make(getWindow().getDecorView(), "发生了一些错误 $_$", Snackbar.LENGTH_INDEFINITE)
                .setAction("返回", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    public void showLoading() {
        binding.setIsLoaded(false);
    }

    @Override
    public void hideLoading() {
        start.setAlpha(1);
        end.setAlpha(1);
        doHideLoadingAnimation().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                binding.setIsLoaded(true);
            }
        });
    }

    public Observable<Integer> doHideLoadingAnimation() {
        final ConnectableObservable<Integer> publish = Observable.just(0).publish();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(binding.topView, "translationY", 0,
                -binding.topView.getHeight()),
                ObjectAnimator.ofFloat(binding.bottomView, "translationY", 0,
                        binding.bottomView.getHeight()),
                ObjectAnimator.ofFloat(binding.imgTracePreview,
                        "alpha", 1, 0));
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                publish.connect();
            }
        });
        set.start();
        return publish;
    }

    @Override
    public void setPresenter(TraceContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
