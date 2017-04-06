package com.congxiaoyao.xber_admin.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.congxiaoyao.xber_admin.R;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class XberDividerLayout extends LinearLayout {

    private int dividerSize;
    private int dividerColor;
    private int dividerPaddingLeft;
    private int dividerPaddingRight;

    private ColorDrawable dividerDrawable = new ColorDrawable();

    public XberDividerLayout(Context context) {
        this(context, null);
    }

    public XberDividerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XberDividerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XberDividerLayout);
        dividerSize = a.getDimensionPixelSize(R.styleable.XberDividerLayout_divider_size, 0);
        dividerColor = a.getColor(R.styleable.XberDividerLayout_divider_color, Color.BLACK);
        dividerPaddingLeft = a.getDimensionPixelSize(R.styleable
                .XberDividerLayout_divider_padding_left, 0);
        dividerPaddingRight = a.getDimensionPixelSize(R.styleable
                .XberDividerLayout_divider_padding_right, 0);
        boolean top = a.getBoolean(R.styleable.XberDividerLayout_divider_top, false);
        boolean bottom = a.getBoolean(R.styleable.XberDividerLayout_divider_bottom, false);
        a.recycle();
        int showDivider = SHOW_DIVIDER_NONE;
        if(top) showDivider = showDivider | SHOW_DIVIDER_BEGINNING;
        if(bottom) showDivider = showDivider | SHOW_DIVIDER_END;
        if (showDivider != SHOW_DIVIDER_NONE) {
            setShowDividers(showDivider);
            setDividerDrawable(new ColorDrawable(dividerColor){
                @Override
                public int getIntrinsicHeight() {
                    return dividerSize;
                }

                @Override
                public void setBounds(Rect bounds) {
                    super.setBounds(bounds);
                    bounds.left += dividerPaddingLeft;
                    bounds.right -= dividerPaddingRight;
                }
            });
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getOrientation() == HORIZONTAL) {
            throw new RuntimeException("暂不支持横向布局");
        }
        super.onDraw(canvas);
        drawDividersVertical(canvas);
    }

    private void drawDividersVertical(Canvas canvas) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                if (hasDividerBeforeChild(layoutParams)) {
                    final LayoutParams lp = (LayoutParams) layoutParams;
                    final int top = child.getTop() - lp.dividerSize;
                    drawHorizontalDivider(canvas, top, lp);
                }
                if (hasDividerAfterChild(layoutParams)) {
                    final LayoutParams lp = (LayoutParams) layoutParams;
                    final int top = child.getBottom();

                    drawHorizontalDivider(canvas, top, lp);
                }
            }
        }
    }

    private boolean hasDividerBeforeChild(ViewGroup.LayoutParams lp) {
        if (!(lp instanceof LayoutParams)) {
            return false;
        }
        LayoutParams layoutParams = (LayoutParams) lp;
        return layoutParams.dividerSize > 0 && layoutParams.dividerTop;
    }

    private boolean hasDividerAfterChild(ViewGroup.LayoutParams lp) {
        if (!(lp instanceof LayoutParams)) {
            return false;
        }
        LayoutParams layoutParams = (LayoutParams) lp;
        return layoutParams.dividerSize > 0 && layoutParams.dividerBottom;
    }

    private void drawHorizontalDivider(Canvas canvas, int top, LayoutParams lp) {
        dividerDrawable.setColor(lp.dividerColor);
        dividerDrawable.setBounds(getPaddingLeft() + lp.dividerPaddingLeft, top,
                getWidth() - getPaddingRight() - lp.dividerPaddingRight, top + lp.dividerSize);
        dividerDrawable.draw(canvas);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        if (getOrientation() == HORIZONTAL) {
            throw new RuntimeException("暂不支持横向布局");
        }
        return new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public class LayoutParams extends LinearLayout.LayoutParams {

        private int dividerColor;
        private int dividerPaddingLeft;
        private int dividerPaddingRight;
        private int dividerSize;
        private boolean dividerTop;
        private boolean dividerBottom;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.XberDividerLayout);
            dividerColor = a.getColor(R.styleable.XberDividerLayout_divider_color,
                    XberDividerLayout.this.dividerColor);
            dividerPaddingLeft = a.getDimensionPixelSize(R.styleable
                            .XberDividerLayout_divider_padding_left,
                    XberDividerLayout.this.dividerPaddingLeft);
            dividerPaddingRight = a.getDimensionPixelSize(R.styleable
                            .XberDividerLayout_divider_padding_right,
                    XberDividerLayout.this.dividerPaddingRight);

            dividerSize = a.getDimensionPixelSize(R.styleable
                    .XberDividerLayout_divider_size, XberDividerLayout.this.dividerSize);
            dividerTop = a.getBoolean(R.styleable.XberDividerLayout_divider_top, false);
            if (dividerTop) {
                topMargin += dividerSize;
            }
            dividerBottom = a.getBoolean(R.styleable.XberDividerLayout_divider_bottom, false);
            if (dividerBottom) {
                bottomMargin += dividerSize;
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
        }
    }
}