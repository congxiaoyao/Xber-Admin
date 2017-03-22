package com.congxiaoyao.xber_admin.utils.post;

import android.view.View;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public class LinkedRunnable implements Runnable {

    public LinkedRunnable child;
    public View view;
    public long delay = 1000;

    @Override
    public void run() {
        call();
        if (child != null) {
            view.postDelayed(child, child.delay);
        }
    }

    protected void call() {

    }
}
