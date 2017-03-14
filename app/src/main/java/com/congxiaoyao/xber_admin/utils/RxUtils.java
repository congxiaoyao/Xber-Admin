package com.congxiaoyao.xber_admin.utils;

import java.util.concurrent.Executor;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/13.
 */

public class RxUtils {

    public static <T> Observable.Transformer toMainThread() {
        Observable.Transformer<T, T> transformer = new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.observeOn(AndroidSchedulers.mainThread());
            }
        };
        return transformer;
    }

    public static <T> Observable.Transformer toIoThread() {
        Observable.Transformer<T, T> transformer = new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.observeOn(Schedulers.io());
            }
        };
        return transformer;
    }

    public static <T> Observable.Transformer toScheduler(final Scheduler scheduler) {
        Observable.Transformer<T, T> transformer = new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.observeOn(scheduler);
            }
        };
        return transformer;
    }
}
