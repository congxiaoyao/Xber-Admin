package com.congxiaoyao.xber_admin.helpers;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
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


import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.databinding.ItemSearchBarBinding;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import java.util.List;

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
    }

    public ItemSearchBarBinding getBinding() {
        return binding;
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
    protected void onTraceCars(List<Long> carIds) {

    }
}
