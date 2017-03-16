package com.congxiaoyao.xber_admin.mvpbase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.congxiaoyao.xber_admin.mvpbase.presenter.PagedListLoadablePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

/**
 * Created by congxiaoyao on 2016/8/27.
 */
public abstract class LoadableListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_loadable_list);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getToolbarTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = getFragment();
        if (!(fragment instanceof ListLoadableView)) {
            throw new RuntimeException(
                    "请遵守SimpleMvpActivity约定使用实现了ListLoadableView接口的Fragment");
        }
        ListLoadableView listLoadableView = (ListLoadableView) fragment;
        getPresenter(listLoadableView);

//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_content, fragment);
//        transaction.commit();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(v -> {
//            listLoadableView.scrollToTop();
//            int dis = (int) fab.getTag();
//            fab.animate().translationY(dis).setDuration(200).start();
//        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    /**
     * toolbar上显示的标题
     * @return
     */
    public abstract String getToolbarTitle();


    /**
     * 通过参数{@param baseView}创建一个Presenter
     * @param baseView 这里的参数是{@link SimpleMvpActivity#getFragment()}方法的返回值
     * @return BasePresenter的实现类
     */
    public abstract PagedListLoadablePresenter getPresenter(ListLoadableView baseView);

    /**
     * @return 一定要返回一个实现了ListLoadableView接口的Fragment 否则将会抛出异常
     */
    public abstract Fragment getFragment();
}
