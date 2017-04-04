package com.congxiaoyao.xber_admin.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class XberSwipeRefreshLayout extends SwipeRefreshLayout {

    private boolean startFlag = false;
    private boolean shouldHandle = false;
    private int totalDy = 0;

    private int maxDy = 36;

    public XberSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public XberSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        maxDy = DisplayUtils.dp2px(context, maxDy);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startFlag = true;
            shouldHandle = false;
            totalDy = 0;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        handleStart(dyUnconsumed);
        if (dyUnconsumed < 0 && shouldHandle) {
            totalDy += -dyUnconsumed;
            if (totalDy > maxDy) {
                target.stopNestedScroll();
                return;
            } else {
                super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            }
            return;
        }
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//        Log.d(TAG.ME, "onNestedScroll: " + String.format("消费:(dy = %d) ", dyConsumed) +
//                String.format("未消费:(dy = %d) ", dyUnconsumed));
    }

    private void handleStart(int dyUnconsumed) {
        if (!startFlag) return;
        if (dyUnconsumed == 0) shouldHandle = true;
        startFlag = false;
    }
}
