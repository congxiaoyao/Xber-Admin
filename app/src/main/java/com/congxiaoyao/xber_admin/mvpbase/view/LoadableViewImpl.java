package com.congxiaoyao.xber_admin.mvpbase.view;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.View;

import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;

/**
 * 带有progressBar处理的view实现 同时加入对登录完成时的回调
 * <p>
 * 可覆写
 * {@link LoadableViewImpl#onReLoginSuccess()}
 * {@link LoadableViewImpl#onReLoginFailed()}
 * 两个方法来对重新登录结束做处理 如自动的重新请求数据等
 *
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.CODE_REQUEST_LOGIN) {
            if (resultCode == LoginActivity.CODE_RESULT_SUCCESS) {
                onReLoginSuccess();
            } else {
                onReLoginFailed();
            }
        }
    }

    protected void onReLoginSuccess() {

    }

    protected void onReLoginFailed() {

    }

    @Override
    public void setPresenter(T presenter) {
        this.presenter = presenter;
    }
}
