package com.congxiaoyao.adapter.base.binding.annotations;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemLayout {
    @LayoutRes int value() default -1;
}
