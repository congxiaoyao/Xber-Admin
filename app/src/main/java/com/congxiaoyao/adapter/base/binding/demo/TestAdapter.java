package com.congxiaoyao.adapter.base.binding.demo;

import android.util.Log;

import com.congxiaoyao.adapter.base.binding.annotations.ItemLayout;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemTestBinding;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class TestAdapter {

    @ItemLayout(R.layout.item_test)
    public void bindData(ItemTestBinding binding, SampleBean bean) {
        long pre = System.nanoTime();
        binding.setBean(bean);
        binding.executePendingBindings();
        Log.d("cxy", "binding time = " + (System.nanoTime() - pre));
    }

}
