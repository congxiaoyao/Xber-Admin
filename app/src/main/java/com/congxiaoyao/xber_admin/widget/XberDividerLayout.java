package com.congxiaoyao.xber_admin.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.congxiaoyao.xber_admin.R;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class XberDividerLayout extends LinearLayout {

    private int dividerSize;
    private Drawable dividerDrawable;
    private boolean enableHeader;
    private boolean enableFooter;

    public XberDividerLayout(Context context) {
        this(context, null);
    }

    public XberDividerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XberDividerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XberDividerLayout);
        dividerSize = a.getDimensionPixelSize(R.styleable.XberDividerLayout_divider_size, 1);
        int color = a.getColor(R.styleable.XberDividerLayout_divider_color, Color.BLACK);
        dividerDrawable = new ColorDrawable(color){
            @Override
            public int getIntrinsicHeight() {
                return dividerSize;
            }
        };
        enableFooter = a.getBoolean(R.styleable.XberDividerLayout_enable_footer, false);
        enableHeader = a.getBoolean(R.styleable.XberDividerLayout_enable_header, false);
        a.recycle();

        setShowDividers(SHOW_DIVIDER_BEGINNING | SHOW_DIVIDER_END);
        setDividerDrawable(dividerDrawable);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getOrientation() == HORIZONTAL) {
            throw new RuntimeException("暂不支持横向布局");
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dividerDrawable == null) {
            return;
        }
        drawDividersVertical(canvas);
    }

    private void drawDividersVertical(Canvas canvas) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt(i)) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int top = child.getTop() - lp.topMargin - dividerSize;
                    drawHorizontalDivider(canvas, top);
                }
            }
        }

        if (hasDividerBeforeChildAt(count)) {
            final View child = getLastNonGoneChild();
            int bottom = 0;
            if (child == null) {
                bottom = getHeight() - getPaddingBottom() - dividerSize;
            } else {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                bottom = child.getBottom() + lp.bottomMargin;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    private boolean hasDividerBeforeChildAt(int childIndex) {
        int childCount = getChildCount();
        if (childIndex == childCount || childIndex == 0) {
            return true;
        }
        return (childIndex == 1 && enableHeader) ||
                (childIndex == (childCount - 1) && enableFooter);
    }

    private View getLastNonGoneChild() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                return child;
            }
        }
        return null;
    }

    private void drawHorizontalDivider(Canvas canvas, int top) {
        dividerDrawable.setBounds(getPaddingLeft() + getDividerPadding(), top,
                getWidth() - getPaddingRight() - getDividerPadding(), top + dividerSize);
        dividerDrawable.draw(canvas);
    }
}