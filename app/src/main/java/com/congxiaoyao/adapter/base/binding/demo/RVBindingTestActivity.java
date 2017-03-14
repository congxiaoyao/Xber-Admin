package com.congxiaoyao.adapter.base.binding.demo;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.adapter.base.binding.BindingAdapterHelper;
import com.congxiaoyao.adapter.base.binding.annotations.ItemLayout;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemTestBinding;

import java.util.ArrayList;
import java.util.List;

public class RVBindingTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rvbinding_test);

        List<SampleBean> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(new SampleBean(ContextCompat.getDrawable(this, R.mipmap.ic_launcher),
                    "text for testing " + i));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        BaseQuickAdapter adapter = BindingAdapterHelper.create(recyclerView)
                .with(new LinearLayoutManager(this))
                .setBindingAdapter(this);
        adapter.addData(list);
    }

    @ItemLayout(R.layout.item_test)
    public void bindItemData(ItemTestBinding binding, SampleBean bean) {
        binding.setBean(bean);
        binding.executePendingBindings();
    }
}
