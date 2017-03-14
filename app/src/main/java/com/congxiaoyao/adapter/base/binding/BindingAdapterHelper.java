package com.congxiaoyao.adapter.base.binding;

import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.congxiaoyao.adapter.base.binding.annotations.ItemLayout;

import java.lang.reflect.Method;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class BindingAdapterHelper {

    private RecyclerView recyclerView;

    public static BindingAdapterHelper create(RecyclerView recyclerView) {
        BindingAdapterHelper helper = new BindingAdapterHelper();
        helper.recyclerView = recyclerView;
        return helper;

    }

    public BindingAdapterHelper with(RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
        return this;
    }

    public BaseQuickAdapter setBindingAdapter(Object adapter) {
        int layoutId = 0;
        Method bindingMethod = null;
        Method[] methods = adapter.getClass().getMethods();
        for (Method method : methods) {
            ItemLayout annotation = method.getAnnotation(ItemLayout.class);
            if (annotation != null) {
                layoutId = annotation.value();
                bindingMethod = method;
                break;
            }
        }
        BindingQuickAdapter bindingQuickAdapter = new BindingQuickAdapter();
        bindingQuickAdapter.method = bindingMethod;
        bindingQuickAdapter.bindingAdapter = adapter;
        bindingQuickAdapter.layoutId = layoutId;
        recyclerView.setAdapter(bindingQuickAdapter);
        return bindingQuickAdapter;
    }
}
