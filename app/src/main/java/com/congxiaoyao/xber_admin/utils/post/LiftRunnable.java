package com.congxiaoyao.xber_admin.utils.post;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public class LiftRunnable implements Runnable {

    public Runnable doSomeThing;

    public LiftRunnable(final LiftRunnable parent, final Runnable doSomeThing) {
        if (doSomeThing == null) {
            this.doSomeThing = new Runnable() {
                @Override
                public void run() {
                    call();
                }
            };
        }else {
            this.doSomeThing = doSomeThing;
        }

        if (parent != null) {
            parent.doSomeThing = new Runnable() {
                @Override
                public void run() {
                    parent.call();
                    LiftRunnable.this.doSomeThing.run();
                }
            };
        }
    }

    @Override
    public void run() {
        doSomeThing.run();
    }

    public void call() {

    }
}
