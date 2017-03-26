package com.congxiaoyao.xber_admin.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;

/**
 * Created by congxiaoyao on 2017/3/23.
 */

public class LoadingLayout extends RelativeLayout {

    static final int CIRCLE_DIAMETER = 40;

    private static final int MAX_ALPHA = 255;

    private static final int SCALE_DOWN_DURATION = 150;
    private static final int ALPHA_ANIMATION_DURATION = 300;

    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private final Animation scaleAnimation;
    private final Animation scaleDownAnimation;
    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;

    public LoadingLayout(Context context) {
        this(context, null);
    }

    public LoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createProgressView();

        scaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                ViewCompat.setScaleX(mCircleView, interpolatedTime);
                ViewCompat.setScaleY(mCircleView, interpolatedTime);
            }
        };
        scaleAnimation.setDuration(ALPHA_ANIMATION_DURATION);

        scaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                ViewCompat.setScaleX(mCircleView, 1 - interpolatedTime);
                ViewCompat.setScaleY(mCircleView, 1 - interpolatedTime);
            }
        };
        scaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        scaleDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCircleView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT);
        int size = DisplayUtils.dp2px(getContext(), CIRCLE_DIAMETER);
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.addRule(CENTER_HORIZONTAL, TRUE);
        mCircleView.setLayoutParams(layoutParams);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        addView(mCircleView);

        mProgress.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorPrimary));
        mProgress.setAlpha(MAX_ALPHA);
        mProgress.start();
    }

    public void showLoading() {
        mCircleView.setVisibility(View.VISIBLE);
        mCircleView.clearAnimation();
        ObjectAnimator.ofFloat(mCircleView, "alpha", 0.2f, 1)
                .setDuration(ALPHA_ANIMATION_DURATION).start();
        mCircleView.startAnimation(scaleAnimation);
    }

    public void hideLoading() {
        mCircleView.clearAnimation();
        mCircleView.startAnimation(scaleDownAnimation);
    }

    public void below(int id, int marginDp) {
        LayoutParams layoutParams = (LayoutParams) mCircleView.getLayoutParams();
        layoutParams.addRule(BELOW, id);
        int margin = DisplayUtils.dp2px(getContext(), marginDp);
        layoutParams.setMargins(0, margin, 0, 0);
        mCircleView.requestLayout();
    }
}
