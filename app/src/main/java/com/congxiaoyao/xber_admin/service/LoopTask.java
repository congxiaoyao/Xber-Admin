package com.congxiaoyao.xber_admin.service;

import android.os.HandlerThread;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/3/11.
 */

public class LoopTask {

    private boolean running = false;
    private Subscription subscription;
    private int delay = 1000;

    protected void onLoop() {

    }

    public boolean isRunning() {
        return running;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void start(int delay) {
        if (running) return;
        running = true;
        this.delay = delay;
        loopAndDelay();
    }

    private void loopAndDelay() {
        if (!running) return;
        onLoop();
        if (!running) return;
        subscription = Observable.timer(delay, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                loopAndDelay();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void stop() {
        running = false;
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
