package com.congxiaoyao.adapter.base.binding;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class BindingQuickAdapter<T> extends BaseQuickAdapter<T,BindingViewHolder> {

    Method method;
    Object bindingAdapter;

    @LayoutRes
    int layoutId;

    public BindingQuickAdapter() {
        super(null);
    }

    @Override
    protected void convert(BindingViewHolder helper, T item) {
        try {
            long pre = System.nanoTime();
            method.invoke(bindingAdapter, helper.binding, item);
            Log.d("cxy", "invoke  time = " + (System.nanoTime() - pre));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected BindingViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate((LayoutInflater) parent.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                layoutId, parent, false);
        BindingViewHolder holder = new BindingViewHolder(binding);
        return holder;
    }
}
