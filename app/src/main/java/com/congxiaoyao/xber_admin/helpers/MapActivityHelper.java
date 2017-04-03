package com.congxiaoyao.xber_admin.helpers;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.VersionUtils;

/**
 * Created by congxiaoyao on 2017/4/2.
 */

public class MapActivityHelper {

    public static void showStatusBar(View statusBar) {
        if (VersionUtils.LOLLIPOP_MR1AndPlus) {
            statusBar.setVisibility(View.VISIBLE);
            statusBar.setBackgroundColor(Color.parseColor("#55000000"));
            ViewGroup.LayoutParams layoutParams = statusBar.getLayoutParams();
            layoutParams.height = DisplayUtils.getStatusBarHeight(statusBar.getContext());
            statusBar.requestLayout();
        }
    }
}
