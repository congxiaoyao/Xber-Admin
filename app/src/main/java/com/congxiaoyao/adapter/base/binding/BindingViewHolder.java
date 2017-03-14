package com.congxiaoyao.adapter.base.binding;

import android.databinding.ViewDataBinding;

import com.chad.library.adapter.base.BaseViewHolder;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class BindingViewHolder extends BaseViewHolder {

    protected ViewDataBinding binding;

    public BindingViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
