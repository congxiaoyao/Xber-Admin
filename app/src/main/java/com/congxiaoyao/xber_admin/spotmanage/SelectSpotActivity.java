package com.congxiaoyao.xber_admin.spotmanage;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.tool.util.StringUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.congxiaoyao.httplib.request.SpotRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ActivitySelectSpotBinding;
import com.congxiaoyao.xber_admin.helpers.MapActivityHelper;
import com.congxiaoyao.xber_admin.utils.BaiduMapUtils;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.Token;
import com.congxiaoyao.xber_admin.utils.VersionUtils;

import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SelectSpotActivity extends AppCompatActivity {

    private ActivitySelectSpotBinding binding;

    public static final int REQUEST_ADD = 0;
    public static final int REQUEST_UPDATE = 1;
    public static final String EXTRA_KEY = "SPOT";
    private BaiduMap baiduMap;

    private ObjectAnimator pinAnimator;

    private ParcelSpot spot;

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_spot);
        progressBar = (ContentLoadingProgressBar) binding.getRoot()
                .findViewById(R.id.content_progress_bar);
        binding.setPresenter(new Presenter());
        MapActivityHelper.showStatusBar(binding.statusBar);
        baiduMap = binding.mapView.getMap();
        binding.mapView.showZoomControls(false);
        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                pinAnimator.start();
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                LatLng latLng = BaiduMapUtils
                        .getScreenCenterLatLng(SelectSpotActivity.this, baiduMap);
                binding.editText.setHint(String.format(Locale.CHINA, " (%.6f,%.6f)",
                        latLng.longitude, latLng.latitude));
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                pinAnimator.reverse();
                binding.editText.setHint(R.string.please_name_it);
            }
        });

        int translationY = -DisplayUtils.dp2px(this, 12);
        pinAnimator = ObjectAnimator.ofFloat(binding.imgLocation, "translationY", 0, translationY);
        pinAnimator.setDuration(100);

        Intent intent = getIntent();
        spot = intent.getParcelableExtra(EXTRA_KEY);
        if (spot != null) {
            BaiduMapUtils.moveToLatLng(baiduMap, spot.getLatitude(), spot.getLongitude(), 18);
            binding.editText.setText(spot.getSpotName());
        }
    }

    public static void startForUpdate(Fragment context, Spot spot) {
        ParcelSpot parcelSpot = new ParcelSpot(spot.getSpotId(),
                spot.getSpotName(), spot.getLatitude(), spot.getLongitude());
        Intent intent = new Intent(context.getContext(), SelectSpotActivity.class);
        intent.putExtra(EXTRA_KEY, parcelSpot);
        context.startActivityForResult(intent, REQUEST_UPDATE);
    }

    public static void startForAdd(Fragment context) {
        Intent intent = new Intent(context.getContext(), SelectSpotActivity.class);
        context.startActivityForResult(intent, REQUEST_ADD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
        subscriptions.unsubscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    public void update(Spot spot) {
        subscribe(XberRetrofit.create(SpotRequest.class)
                .updateSpot(spot, Token.value));
    }

    public void add(Spot spot) {
        subscribe(XberRetrofit.create(SpotRequest.class)
                .addSpot(spot, Token.value));
    }

    public void subscribe(Observable<String> observable) {
        showLoading();
        Subscription subscription = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(SelectSpotActivity.this, s, Toast.LENGTH_SHORT).show();
                        hideLoading();
                        setResult(RESULT_OK);
                        finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("cxy", "call: ", throwable);
                        Toast.makeText(SelectSpotActivity.this, "发生了一些错误...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        subscriptions.add(subscription);
    }

    public class Presenter{

        public void onClick(View view) {
            String name = binding.editText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(SelectSpotActivity.this, "请填写地址名称", Toast.LENGTH_SHORT).show();
                return;
            }
            long id = (spot == null ? 0 : spot.getSpotId());
            LatLng latLng = BaiduMapUtils.getScreenCenterLatLng(SelectSpotActivity.this, baiduMap);
            Spot newSpot = new Spot(id, name, latLng.latitude, latLng.longitude);
            if (spot == null) {
                add(newSpot);
            }else {
                update(newSpot);
            }
        }

        public void onSearch(View view) {
            startActivityForResult(new Intent(SelectSpotActivity.this, SearchPoiActivity.class), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SuggestionInfo info = data.getParcelableExtra(SearchPoiActivity.EXTRA_KEY);
        if (info != null) {
            BaiduMapUtils.moveToLatLng(baiduMap, info.pt.latitude, info.pt.longitude, 18);
            binding.editText.setText(info.key);
        }
    }

    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoading() {
        if (progressBar != null) {
            progressBar.hide();
        }
    }
}
