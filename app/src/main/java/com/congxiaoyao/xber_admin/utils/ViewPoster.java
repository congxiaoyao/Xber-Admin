package com.congxiaoyao.xber_admin.utils;

import android.view.View;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public class ViewPoster {

    private Observable<View> observable;

    private ViewPoster() {}

    public static ViewPoster from(final View view) {
        ViewPoster poster = new ViewPoster();
        poster.observable = Observable.create(new Observable.OnSubscribe<View>() {
            @Override
            public void call(final Subscriber<? super View> subscriber) {
                subscriber.onNext(view);
                subscriber.onCompleted();
            }
        });
        return poster;
    }

    public ViewPoster postDelay(final Runnable runnable, final long delay) {
        observable = observable.flatMap(new Func1<View, Observable<View>>() {
            @Override
            public Observable<View> call(final View view) {
                final Subscriber<View>[] sub = new Subscriber[]{null};
                Observable<View> observable = Observable.create(new Observable.OnSubscribe<View>() {
                    @Override
                    public void call(Subscriber<? super View> subscriber) {
                        sub[0] = (Subscriber<View>) subscriber;
                    }
                });
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        sub[0].onNext(view);
                        sub[0].onCompleted();
                    }
                }, delay);
                return observable;
            }
        });
        return this;
    }

    public ViewPoster post(Runnable runnable) {
        return postDelay(runnable, 0);
    }

    public void start() {
        observable.subscribe(new Action1<View>() {
            @Override
            public void call(View view) {

            }
        });
    }
}
