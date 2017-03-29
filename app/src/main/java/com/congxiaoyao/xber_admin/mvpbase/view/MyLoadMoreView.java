package com.congxiaoyao.xber_admin.mvpbase.view;

import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.congxiaoyao.xber_admin.R;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class MyLoadMoreView extends LoadMoreView {

    @Override
    public int getLayoutId() {
        return R.layout.view_load_more;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.content_progress_bar;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.tv_load_failed;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.tv_load_end;
    }
}
