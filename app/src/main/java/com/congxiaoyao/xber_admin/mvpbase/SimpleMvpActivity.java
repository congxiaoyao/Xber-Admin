package com.congxiaoyao.xber_admin.mvpbase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.BaseView;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * 一个简单的只带一个toolbar的Activity 里面的内容由Fragment展示
 * 此Activity的实现遵循Mvp设计模式 将view的内容全部交由Fragment来处理
 * 同时负责初始化Presenter 设置基本的返回按钮监听和程序标题
 * <p>
 * Created by congxiaoyao on 2016/8/25.
 */
public abstract class SimpleMvpActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getToolbarTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = getFragment();
        if (!(fragment instanceof BaseView)) {
            throw new RuntimeException("请遵守SimpleMvpActivity约定使用实现了BaseView接口的Fragment");
        }
        BaseView baseView = (BaseView) fragment;
        getPresenter(baseView);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, fragment);
        transaction.commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    /**
     * toolbar上显示的标题
     *
     * @return
     */
    public abstract String getToolbarTitle();

    /**
     * 通过参数{@param baseView}创建一个Presenter
     *
     * @param baseView 这里的参数是{@link SimpleMvpActivity#getFragment()}方法的返回值
     * @return BasePresenter的实现类
     */
    public abstract BasePresenter getPresenter(BaseView baseView);

    /**
     * @return 一定要返回一个实现了BaseView接口的Fragment 否则将会抛出异常
     */
    public abstract Fragment getFragment();

}