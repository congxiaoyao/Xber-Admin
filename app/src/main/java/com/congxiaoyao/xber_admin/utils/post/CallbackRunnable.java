package com.congxiaoyao.xber_admin.utils.post;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public abstract class CallbackRunnable implements Runnable {

    @Override
    public void run() {
        myRun();
        callback();
    }

    protected abstract void myRun();

    protected void callback() {

    }
}
