package com.congxiaoyao.xber_admin.utils.post;

import android.view.View;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public class ViewPoster {

    private View view;
    private LiftRunnable runnable;
    private LiftRunnable header;

    private ViewPoster() {}

    public static ViewPoster from(View view) {
        ViewPoster poster = new ViewPoster();
        poster.view = view;
        poster.runnable = new LiftRunnable(null, null);
        poster.header = poster.runnable;
        return poster;
    }

    public ViewPoster post(final Runnable runnable) {
        final LiftRunnable child = new LiftRunnable(this.runnable, null) {
            @Override
            public void call() {
                runnable.run();
            }
        };
        child.doSomeThing = new Runnable() {
            @Override
            public void run() {
                view.postDelayed(child, 1000);
            }
        };
        ViewPoster poster = new ViewPoster();
        poster.view = this.view;
        poster.runnable = child;
        poster.header = header;
        return poster;
    }

    public void start() {
        view.post(header);
    }
}
