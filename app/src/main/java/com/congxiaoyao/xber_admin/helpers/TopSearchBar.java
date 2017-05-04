package com.congxiaoyao.xber_admin.helpers;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.databinding.ViewDataBinding;


import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.httplib.request.LocationRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.CarPosition;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.xber_admin.MainActivity;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.databinding.ItemSearchBarBinding;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.MathUtils;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public class TopSearchBar {

    public static final int ICON_STATE_SEARCH = 0;
    public static final int ICON_STATE_CANCEL = 1;
    protected ItemSearchBarBinding binding;
    protected LinearLayout animationLayer;
    protected boolean isAnimating = false;

    protected TimeInterpolator defaultInterpolator = new DecelerateInterpolator();

    private int iconState = ICON_STATE_SEARCH;
    protected boolean enabled;
    private Subscription subscribe;

    public TopSearchBar(ItemSearchBarBinding binding, final LinearLayout animationLayer) {
        this.binding = binding;
        this.animationLayer = animationLayer;
        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iconState == ICON_STATE_SEARCH) {
                    onSearchClick();
                } else {
                    onCancelClick();
                }
            }
        });
        binding.tvHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iconState == ICON_STATE_CANCEL) {
                    onDetailClick();
                }else {
                    onSearchClick();
                }
            }
        });
    }

    /**
     * 暴露给外界的与DrawerLayout关联的方法
     * @param drawerLayout
     */
    public void setupWithDrawerLayout(final DrawerLayout drawerLayout) {
        binding.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }, 200);
            }
        });
    }

    /**
     * 给子类用的设置图标
     */
    protected void setRightIconSearch() {
        binding.imgRightIcon.setImageResource(R.drawable.abc_ic_search_api_mtrl_alpha);
        iconState = ICON_STATE_SEARCH;
    }

    /**
     * 给子类用的设置图标
     */
    protected void setRightIconCancel() {
        binding.imgRightIcon.setImageResource(R.drawable.abc_ic_clear_mtrl_alpha);
        iconState = ICON_STATE_CANCEL;
    }

    protected ItemSearchBarBinding openAnimationLayer() {
        animationLayer.removeAllViews();
        ItemSearchBarBinding orgViewBinding = DataBindingUtil.inflate(LayoutInflater
                .from(binding.getRoot().getContext()), R.layout.item_search_bar, null, false);
        ViewGroup.LayoutParams layoutParams = orgViewBinding.cardView.getLayoutParams();
        layoutParams.height = DisplayUtils.dp2px(animationLayer.getContext(), 48);
        animationLayer.addView(orgViewBinding.getRoot());
        animationLayer.setVisibility(View.VISIBLE);
        return orgViewBinding;
    }

    protected void hideSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    protected void shakeCard(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, DisplayUtils.dp2px(binding.cardView.getContext(), 4)).setDuration(200);
        animator.setInterpolator(new CycleInterpolator(1.5f));
        animator.start();
    }

    public int getIconState() {
        return iconState;
    }

    public void setIconScale(float scale) {
        if (scale > 1.1f) scale = 1.1f;
        if (scale < 1.0f) scale = 1.0f;
        scale = MathUtils.map(1.0f, 1.1f, 1.0f, 1.2f, scale);
        binding.imgRightIcon.setScaleX(scale);
        binding.imgRightIcon.setScaleY(scale);
    }

    public void setHintScale(float scale) {
        if (scale > 1.1f) scale = 1.1f;
        if (scale < 1.0f) scale = 1.0f;
        scale = MathUtils.map(1.0f, 1.1f, 1.0f, 1.2f, scale);
        binding.tvHint.setScaleX(scale);
        binding.tvHint.setScaleY(scale);
    }

    public void changeIconColorRed(float value) {
        float maxValue = 1.1f;
        float minValue = 1f;
        int gray = ContextCompat.getColor(binding.getRoot().getContext(), R.color.colorDarkGray);
        int red = ContextCompat.getColor(binding.getRoot().getContext(), R.color.colorDarkRed);
        if(value > maxValue) {
            binding.imgRightIcon.getDrawable().setTint(red);
            return;
        }
        if (value < minValue) {
            binding.imgRightIcon.getDrawable().setTint(gray);
            return;
        }
        int r = (int) MathUtils.map(minValue, maxValue, Color.red(gray), Color.red(red), value);
        int g = (int) MathUtils.map(minValue, maxValue, Color.green(gray), Color.green(red), value);
        int b = (int) MathUtils.map(minValue, maxValue, Color.blue(gray), Color.blue(red), value);
        int result = Color.rgb(r, g, b);
        binding.imgRightIcon.getDrawable().setTint(result);
        binding.imgLeftIcon.getDrawable().setTint(result);
        binding.tvHint.setTextColor(result);
    }

    public ItemSearchBarBinding getBinding() {
        return binding;
    }

    @NonNull
    public static LatLngBounds carPositionToLatLngBounds(CarPosition carPosition) {
        return new LatLngBounds.Builder()
                .include(new LatLng(carPosition.getLat() - 0.06,
                        carPosition.getLng() - 0.06))
                .include(new LatLng(carPosition.getLat() + 0.06,
                        carPosition.getLng() + 0.06)).build();
    }


    protected void moveMapToCarPosition(final Context context, final List<Long> carIds,
                                        final LatLngBounds defBounds) {
        Observable<LatLngBounds> netWork = XberRetrofit.create(LocationRequest.class)
                .getRunningCars(carIds, Token.value)
                .map(new Func1<List<CarPosition>, LatLngBounds>() {
                    @Override
                    public LatLngBounds call(List<CarPosition> carPositions) {
                        if (carPositions == null || carPositions.size() == 0) {
                            throw new EmptyDataException();
                        }
                        if (carPositions.size() == 1) {
                            return carPositionToLatLngBounds(carPositions.get(0));
                        }
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (CarPosition carPosition : carPositions) {
                            builder.include(new LatLng(carPosition.getLat(),
                                    carPosition.getLng()));
                        }
                        return builder.build();
                    }
                });
        Observable<LatLngBounds> local = Observable.just(defBounds)
                .delay(2000, TimeUnit.MILLISECONDS);
        subscribe = Observable.merge(netWork, local).take(1)
                .compose(RxUtils.<LatLngBounds>defaultScheduler())
                .subscribe(new Action1<LatLngBounds>() {
                    @Override
                    public void call(LatLngBounds latLngBounds) {
                        MainActivity.moveMap(context, latLngBounds);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (defBounds != null) {
                            MainActivity.moveMap(context, defBounds);
                        }
                    }
                });
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 当点击到内容外侧
     */
    protected void onClickOutSide() {

    }

    /**
     * 当点集返回键时的处理
     */
    public boolean onBackPressed() {
        return false;
    }

    /**
     * 监听回调可覆写
     */
    protected void onSearchClick() {

    }

    /**
     * 监听回调可覆写
     */
    protected void onCancelClick() {

    }

    /**
     * 监听点击详情按钮
     */
    protected void onDetailClick() {

    }

    /**
     * 数据请求完成开始追踪时的回调
     *
     * @param carIds 当carIds为null时 意味着取消被点击了
     */
    protected void onTraceCars(List<Long> carIds, LatLngBounds bounds) {

    }

    public void destroy() {
        if (subscribe != null) {
            subscribe.unsubscribe();
        }
    }
}
