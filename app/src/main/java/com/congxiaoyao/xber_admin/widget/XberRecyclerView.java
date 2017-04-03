package com.congxiaoyao.xber_admin.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by congxiaoyao on 2016/7/28.
 */
public class XberRecyclerView extends RecyclerView {

    private static final String TAG = "cxy";
    private boolean tryToStopIt = true;
    private float lastY = 0;


    public XberRecyclerView(Context context) {
        this(context, null);
    }

    public XberRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XberRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            stopScroll();
        }
        return super.onInterceptTouchEvent(e);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        if (!(getLayoutManager() instanceof LinearLayoutManager)) return super.onTouchEvent(e);
//
//        int action = e.getAction();
//        int state = getScrollState();
//        LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
//        int position = manager.findFirstCompletelyVisibleItemPosition();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                tryToStopIt = (position != 0);
//                lastY = e.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
////                if (e.getRawY() - lastY < 0) {
////                    stopNestedScroll();
////                    break;
////                }
//                if (state == SCROLL_STATE_DRAGGING && position == 0 && tryToStopIt) {
//                    stopNestedScroll();
//                }
//                lastY = e.getRawY();
//                break;
//        }
//        return super.onTouchEvent(e);
//    }
}
