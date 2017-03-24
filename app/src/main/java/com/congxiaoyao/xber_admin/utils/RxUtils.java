package com.congxiaoyao.xber_admin.utils;

import android.util.Log;
import android.view.View;

import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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

    /**
     * 从构造transformer对象开始 到执行延时方法的时候 如果整个过程够参数delay那么长时间
     * 则将等待直到达到delay时间
     *
     * @param delay 毫秒数
     * @return
     */
    public static <T> Observable.Transformer<T, T> delayWhenTimeEnough(final long delay) {
        final long pre = System.currentTimeMillis();
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.flatMap(new Func1<T, Observable<T>>() {
                    @Override
                    public Observable<T> call(T t) {
                        Observable<T> observable = Observable.just(t);
                        long now = System.currentTimeMillis();
                        if (now - pre < delay) {
                            long realDelay = delay - now + pre;
                            observable = observable.delay(realDelay, TimeUnit.MILLISECONDS);
                        }
                        return observable;
                    }
                });
            }
        };
    }

    public static <T> Observable.Transformer<T, T> post(final View view, final Action1<T> action1) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.doOnNext(new Action1<T>() {
                    @Override
                    public void call(final T t) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                action1.call(t);
                            }
                        });
                    }
                });
            }
        };
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
