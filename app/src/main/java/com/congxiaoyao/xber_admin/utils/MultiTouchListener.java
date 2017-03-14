package com.congxiaoyao.xber_admin.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

import com.congxiaoyao.location.utils.RoundList;

/**
 * Created by congxiaoyao on 2017/1/24.
 */

public class MultiTouchListener implements View.OnTouchListener {

    private RoundList<Long> roundList;

    public MultiTouchListener() {
        roundList = new RoundList<>(2);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            roundList.add(System.currentTimeMillis());
            if (roundList.size() != 2) return false;
            long t1 = roundList.get(0);
            long t2 = roundList.get(1);
            if (t2 - t1 < 500) {
                roundList.removeAll();
                onMultiTouch();
            }
        }
        return false;
    }

    public void onMultiTouch() {

    }
}
