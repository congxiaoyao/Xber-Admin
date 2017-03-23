package com.congxiaoyao.xber_admin.dispatch;

import android.app.Activity;
import android.widget.Toast;

import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by guo on 2017/3/23.
 */

public class DistributePresenterImpl extends BasePresenterImpl<DistributeContract.View>
        implements DistributeContract.Presenter {

    public DistributePresenterImpl(DistributeContract.View view) {
        super(view);
    }

    @Override
    public void subscribe() {
        view.showLoading();
        Subscription subscribe = XberRetrofit.create(TaskRequest.class)
                .generateTask(((DispatchTaskActivity) view.getContext()).getData(), Token.value)
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        view.hideLoading();
                        Toast.makeText(view.getContext(), s, Toast.LENGTH_SHORT).show();
                        ((Activity) view.getContext()).finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        exceptionDispatcher.dispatchException(throwable);
                    }
                });
        subscriptions.add(subscribe);
    }
}
