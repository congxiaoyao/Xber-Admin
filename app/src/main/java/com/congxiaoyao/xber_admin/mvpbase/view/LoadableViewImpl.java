package com.congxiaoyao.xber_admin.mvpbase.view;

import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.View;

import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;

/**
 * Created by congxiaoyao on 2016/8/25.
 */
public class LoadableViewImpl<T extends BasePresenter> extends Fragment implements LoadableView<T> {

    protected T presenter;
    protected ContentLoadingProgressBar progressBar;

    @Override
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.hide();
        }
    }

    @Override
    public void onDestroy() {
        if (presenter != null) {
            presenter.unSubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void setPresenter(T presenter) {
        this.presenter = presenter;
    }
}
