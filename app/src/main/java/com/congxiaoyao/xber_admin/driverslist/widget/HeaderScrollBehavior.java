package com.congxiaoyao.xber_admin.driverslist.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import java.util.Locale;

/**
 * Created by congxiaoyao on 2017/3/31.
 */

public class HeaderScrollBehavior extends CoordinatorLayout.Behavior<View> {

    private CollapsibleHeader header;
    private int minBottom = -1;
    private int maxBottom = -1;

    public HeaderScrollBehavior() {

    }

    public HeaderScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof CollapsibleHeader) {
            header = (CollapsibleHeader) context;
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency.getId() == R.id.appbar;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (header == null) {
            return false;
        }
        if (minBottom == -1) {
            if (header.getToolbarHeight() == 0 || header.getTabBarHeight() == 0
                    || header.getAppbarHeight() == 0) {
                return false;
            }
            maxBottom = header.getAppbarHeight();
            minBottom = header.getToolbarHeight() + header.getTabBarHeight();
        }
        int currentBottom = dependency.getBottom();
        int bgAlpha = (int) MathUtils.map(maxBottom, minBottom, 0, 255, currentBottom);
        header.getBackground().setAlpha(bgAlpha);
        if (bgAlpha < 50) {
            float driverAlpha = MathUtils.map(0, 50, 1, 0, bgAlpha);
            header.getDriverView().setAlpha(driverAlpha);
        }else {
            header.getDriverView().setAlpha(0);
        }
        return true;
    }
}
