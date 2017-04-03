package com.congxiaoyao.xber_admin.driverslist.widget;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by congxiaoyao on 2017/3/31.
 */

public interface CollapsibleHeader {

    int getToolbarHeight();

    int getTabBarHeight();

    int getAppbarHeight();

    View getDriverView();

    Drawable getBackground();
}
