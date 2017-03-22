package com.congxiaoyao.xber_admin.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class CustomViewPager extends ViewPager {

    private boolean isScrollEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && !isScrollEnabled) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            isScrollEnabled = true;
        }
        return  super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isScrollEnabled && super.onInterceptTouchEvent(event);
    }

    public void setScrollEnabled(boolean b) {
        this.isScrollEnabled = b;
    }
}