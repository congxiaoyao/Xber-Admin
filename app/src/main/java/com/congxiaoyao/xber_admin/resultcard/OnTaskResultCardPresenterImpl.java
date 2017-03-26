package com.congxiaoyao.xber_admin.resultcard;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/22.
 */

public class OnTaskResultCardPresenterImpl extends BasePresenterImpl<OnTaskResultCardContract.View>
        implements OnTaskResultCardContract.Presenter, Action1<List<CarDetail>> {

    private Spot start;
    private Spot end;
    private Action1<List<CarDetail>> callback;

    public OnTaskResultCardPresenterImpl(OnTaskResultCardContract.View view) {
        super(view);
    }

    @Override
    public void getCarOnTask(Spot start, Spot end, Action1<List<CarDetail>> callback) {
        this.start = start;
        this.end = end;
        this.callback = callback;
        subscribe();
    }

    @Override
    public void subscribe() {
        Long startId = start == null ? null : start.getSpotId();
        Long endId = end == null ? null : end.getSpotId();
        final long pre = System.currentTimeMillis();
        Subscription subscribe = XberRetrofit.create(CarRequest.class)
                .getCarsOnTask(startId, endId, Token.value)
                .doOnNext(new Action1<List<CarDetail>>() {
                    @Override
                    public void call(List<CarDetail> carDetails) {
                        int minTime = 400;
                        if (System.currentTimeMillis() - pre < minTime) {
                            try {
                                Thread.sleep(minTime - System.currentTimeMillis() + pre);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        exceptionDispatcher.dispatchException(throwable);
                    }
                });
        subscriptions.add(subscribe);
    }

    @Override
    public void destroy(Runnable callback) {
        unSubscribe();
        view.hideMySelf(callback);
    }

    @Override
    public void call(final List<CarDetail> carDetails) {
        final Runnable runnable = callback == null ? null : new Runnable() {
            @Override
            public void run() {
                callback.call(carDetails);
            }
        };
        Observable.just(1).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        view.hideLoading();
                        view.showSuccess();
                    }
                }).observeOn(Schedulers.io()).delay(600, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                view.hideMySelf(runnable);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                exceptionDispatcher.dispatchException(throwable);
            }
        });
    }
}
