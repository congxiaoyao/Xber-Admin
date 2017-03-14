package com.congxiaoyao.xber_admin.mvpbase.view;

import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.congxiaoyao.xber_admin.R;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class MyLoadMoreView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return R.layout.item_load_more;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.content_loading_progress;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override
    protected int getLoadEndViewId() {
        return 0;
    }
}
