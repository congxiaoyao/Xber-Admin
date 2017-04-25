package com.congxiaoyao.xber_admin.monitoring.carinfo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.View;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.DialogCarDetailBinding;
import com.congxiaoyao.xber_admin.widget.BottomDialog;

import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/4/25.
 */

public class CarInfoView implements CarInfoContract.View {

    private CarInfoContract.Presenter presenter;
    private Activity activity;
    private DialogCarDetailBinding binding;

    private ContentLoadingProgressBar loadingView;
    private BottomDialog bottomDialog;

    public CarInfoView(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void showContentView() {
        bottomDialog = new BottomDialog(activity);
        binding = DialogCarDetailBinding.inflate(activity.getLayoutInflater());
        loadingView = (ContentLoadingProgressBar) binding.getRoot().findViewById(R.id.content_progress_bar);
        bottomDialog.setContentView(binding.getRoot());
        bottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                presenter.unSubscribe();
            }
        });
        bottomDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                presenter.unSubscribe();
            }
        });
        binding.setPresenter(presenter);
        presenter.subscribe();
        bottomDialog.show();
    }

    @Override
    public void hideContentView() {
        if (bottomDialog != null) {
            bottomDialog.dismiss();
        }
    }

    @Override
    public void bindData(CarDetail carDetail) {
        if (binding != null) {
            binding.setData(carDetail);
        }
    }

    @Override
    public void showLoading() {
        if(loadingView != null) loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        if(loadingView != null) loadingView.hide();
    }

    @Override
    public void setPresenter(CarInfoContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return activity;
    }
}
