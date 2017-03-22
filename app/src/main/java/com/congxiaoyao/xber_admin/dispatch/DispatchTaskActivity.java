package com.congxiaoyao.xber_admin.dispatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.congxiaoyao.httplib.request.body.LaunchTaskRequest;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.BaseView;

import java.util.ArrayList;
import java.util.List;

public class DispatchTaskActivity extends AppCompatActivity {

    protected Class[] classes;
    private LinearLayout linearLayout;
    private List<ChooseTimeFragment.DateView> listDate;
    private List<String> transportContent;
    private List<String> locations;

    private LaunchTaskRequest request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getToolbarTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        linearLayout = (LinearLayout) findViewById(R.id.week_line);
        classes = new Class[]{};
        transportContent = new ArrayList<>();
        locations = new ArrayList<>();

        Fragment fragment = getFragment();
//        if (!(fragment instanceof BaseView)) {
//            throw new RuntimeException("请遵守SimpleMvpActivity约定使用实现了BaseView接口的Fragment");
//        }
//        BaseView baseView = (BaseView) fragment;
//        getPresenter(baseView);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, fragment);
        transaction.commit();


    }

    public String getToolbarTitle() {
        return "选择可用车辆";
    }

    public BasePresenter getPresenter(BaseView baseView) {
        return new StartLocationPresenterImpl((StratLocationContract.View) baseView);
    }

    public Fragment getFragment() {
        return new ChooseTimeFragment();
    }

    public void jumpToNext(Fragment fragment) {

    }

    public void showWeekLine() {
        linearLayout.setVisibility(View.VISIBLE);
    }

    public void addDate(List<ChooseTimeFragment.DateView> listDate) {
        this.listDate = listDate;
    }

    public List<ChooseTimeFragment.DateView> getDate() {
        if (listDate==null) throw new NullPointerException("ChooseTimeFragment 未调用 addDate方法");
        return listDate;
    }

    public void setToolBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void addTransportContent(String content, String remark) {
        transportContent.add(content);
        transportContent.add(remark);
    }

    public void addLocation(String location) {
        locations.add(location);
    }

    public List<String> getLocations() {
        return locations;
    }

}
