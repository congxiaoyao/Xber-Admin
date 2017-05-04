package com.congxiaoyao.xber_admin.settings;

import android.content.Context;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.XberApplication;

import net.orange_box.storebox.annotations.method.DefaultValue;
import net.orange_box.storebox.annotations.method.KeyByResource;

/**
 * Created by congxiaoyao on 2017/5/3.
 */

public interface Settings {

    @KeyByResource(R.string.setting_max_car_count)
    @DefaultValue(R.integer.def_max_car_count)
    int maxCarCount();

    @KeyByResource(R.string.setting_animate_thread_count)
    @DefaultValue(R.integer.def_animate_thread_count)
    int animateThreadCount();

    @KeyByResource(R.string.setting_notification)
    boolean enableNotification();

    class SettingsHelper {
        public static Settings getInstance(Context context) {
            return ((XberApplication) context.getApplicationContext()).getSettings();
        }
    }
}

