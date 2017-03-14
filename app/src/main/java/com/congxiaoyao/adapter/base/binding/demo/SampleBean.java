package com.congxiaoyao.adapter.base.binding.demo;

import android.graphics.drawable.Drawable;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class SampleBean {

    private Drawable iconId;
    private String text;

    public SampleBean(Drawable iconId, String text) {
        this.iconId = iconId;
        this.text = text;
    }

    public Drawable getIconId() {
        return iconId;
    }

    public void setIconId(Drawable iconId) {
        this.iconId = iconId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "SampleBean{" +
                "iconId=" + iconId +
                ", text='" + text + '\'' +
                '}';
    }
}
