package com.congxiaoyao.xber_admin.widget;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 手动覆写TextView的onTouchEvent方法 使得在不设置clickable属性为true的时候也可以响应点击事件
 * 避免了滑动冲突 使得其可以在viewpager中正常的使用
 * 同时加入了对ripple效果的支持
 *
 * Created by congxiaoyao on 2017/3/17.
 */

public class ClickableRippleTextView extends TextView {

    private RectF rect;
    private RippleDrawable rippleBg;
    private static final int[] STATE_STOP = {android.R.attr.state_enabled};
    private static final int[] STATE_START = {android.R.attr.state_pressed, android.R.attr.state_enabled};

    public ClickableRippleTextView(Context context) {
        super(context);
    }

    public ClickableRippleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableRippleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClickableRippleTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (rect == null) {
            rect = new RectF();
        }
        rect.set(left, top, right, bottom);

        if (getBackground() instanceof RippleDrawable) {
            rippleBg = (RippleDrawable) getBackground();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startEffect();
                trySetRippleHotspot(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isInside(event)) {
                    stopEffect();
                    return false;
                } else {
                    trySetRippleHotspot(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isInside(event)) {
                    callOnClick();
                }
                stopEffect();
                break;
            case MotionEvent.ACTION_CANCEL:
                stopEffect();
                break;
        }
        return true;
    }

    private boolean isInside(MotionEvent event) {
        return rect.contains(event.getX(), event.getY());
    }

    private void stopEffect() {
        Drawable background = getBackground();
        background.setState(STATE_STOP);
    }

    private void startEffect() {
        Drawable background = getBackground();
        background.setState(STATE_START);
    }

    private void trySetRippleHotspot(MotionEvent event) {
        if (rippleBg != null) {
            rippleBg.setHotspot(event.getX(), event.getY());
        }
    }
}