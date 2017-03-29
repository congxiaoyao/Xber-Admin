package com.congxiaoyao.xber_admin.mvpbase.view;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.view.View;
import android.view.ViewGroup;

import com.congxiaoyao.location.utils.RoundList;
import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;

import java.lang.reflect.Field;

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

    protected void listenToolbarDoubleClick() {
        final RoundList<Long> record = new RoundList<>(2);
        if (getContext() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getContext();
            ActionBar supportActionBar = activity.getSupportActionBar();
            Class<? extends ActionBar> clz = supportActionBar.getClass();
            if (!clz.getSimpleName().equals("ToolbarActionBar")) return;
            try {
                Field field = clz.getDeclaredField("mDecorToolbar");
                field.setAccessible(true);
                ToolbarWidgetWrapper toolbarWrapper = (ToolbarWidgetWrapper) field.get(supportActionBar);
                field = toolbarWrapper.getClass().getDeclaredField("mToolbar");
                field.setAccessible(true);
                Toolbar toolbar = (Toolbar) field.get(toolbarWrapper);
                toolbar.setClickable(true);
                toolbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        record.add(System.currentTimeMillis());
                        if (record.size() == 2 && record.get(1) - record.get(0) < 500) {
                            onToolbarDoubleClick();
                            record.removeAll();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onToolbarDoubleClick() {

    }

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
