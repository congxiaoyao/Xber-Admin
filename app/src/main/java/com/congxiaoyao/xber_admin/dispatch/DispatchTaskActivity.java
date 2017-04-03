package com.congxiaoyao.xber_admin.dispatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.congxiaoyao.httplib.request.body.LaunchTaskRequest;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

public class DispatchTaskActivity extends SwipeBackActivity {

    private LinearLayout linearLayout;
    private String today;
    private LaunchTaskRequest request = new LaunchTaskRequest();
    private CarDetail car;
    private Spot start_spot;
    private Spot end_spot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        linearLayout = (LinearLayout) findViewById(R.id.week_line);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, new ChooseTimeFragment());
        transaction.commit();
    }

    public void jumpToNext(Fragment fragment) {
        if (fragment.getClass().equals(ChooseTimeFragment.class)) {
            ChooseStartLocationFragment startLocationFragment = new ChooseStartLocationFragment();
            StratLocationContract.Presenter presenter = new StartLocationPresenterImpl(startLocationFragment, 0);
            getSupportActionBar().setTitle("请选择起始地点");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_content, startLocationFragment).addToBackStack(null);
            transaction.setTransition(TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        } else if (fragment.getClass().equals(ChooseStartLocationFragment.class)) {
            if (((ChooseStartLocationFragment) fragment).getPresenter().getType() == 0) {
                getSupportActionBar().setTitle("请选择目的地点");
                ChooseStartLocationFragment endLocationFragment = new ChooseStartLocationFragment();
                StratLocationContract.Presenter presenter = new StartLocationPresenterImpl(endLocationFragment, 1);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_content, endLocationFragment).addToBackStack(null);
                ;
                transaction.setTransition(TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
            } else if (((ChooseStartLocationFragment) fragment).getPresenter().getType() == 1) {
                getSupportActionBar().setTitle("请选择司机");
                DispatchFragment dispatchFragment = new DispatchFragment();
                DispatchPresenterImpl dispatchPresenter = new DispatchPresenterImpl(dispatchFragment);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_content, dispatchFragment).addToBackStack(null);
                ;
                transaction.setTransition(TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
            }
        } else if (fragment.getClass().equals(DispatchFragment.class)) {
            getSupportActionBar().setTitle("运输内容");
            TransportFragment transportFragment = new TransportFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_content, transportFragment).addToBackStack(null);
            ;
            transaction.setTransition(TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        } else if (fragment.getClass().equals(TransportFragment.class)) {
            getSupportActionBar().setTitle("派发任务");
            DistributeFragment distributeFragment = new DistributeFragment();
            DistributePresenterImpl presenter = new DistributePresenterImpl(distributeFragment);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_content, distributeFragment).addToBackStack(null);
            ;
            transaction.setTransition(TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void notifyToolBar(Fragment fragment) {
        if (fragment.getClass().equals(ChooseStartLocationFragment.class)) {
            if (((ChooseStartLocationFragment) fragment).getPresenter().getType() == 0) {
                setToolBarTitle(today);
            } else {
                setToolBarTitle("请选择起始地点");
            }
        } else if (fragment.getClass().equals(DispatchFragment.class)) {
            setToolBarTitle("请选择目的地点");
        } else if (fragment.getClass().equals(TransportFragment.class)) {
            setToolBarTitle("请选择司机");
        } else if (fragment.getClass().equals(DistributeFragment.class)) {
            setToolBarTitle("运输内容");
        }
    }

    public void showWeekLine() {
        linearLayout.setVisibility(View.VISIBLE);
    }

    public void hideWeekLine() {
        linearLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public void setToolBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public View showToolbarButton() {
        View view = findViewById(R.id.btn_today);
        view.setVisibility(View.VISIBLE);
        return view;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public void hideToolbarButton() {
        findViewById(R.id.btn_today).setVisibility(View.GONE);
    }

    public void setStartTime(Long startTime) {
        request.setStartTime(startTime);
    }

    public void setEndTime(Long endTime) {
        request.setEndTime(endTime);
    }

    public void setStartSpot(Spot startSpot) {
        start_spot = startSpot;
        request.setStartSpot(startSpot.getSpotId());
    }

    public void setEndSpot(Spot endSpot) {
        end_spot = endSpot;
        request.setEndSpot(endSpot.getSpotId());
    }

    public void setCarId(Long carId) {
        request.setCarId(carId);
    }

    public void setCar(CarDetail car) {
        this.car = car;
    }

    public void setContent(String content) {
        request.setContent(content);
    }

    public void setNote(String note) {
        request.setNote(note);
    }

    public LaunchTaskRequest getData() {
        return request;
    }

    public CarDetail getCar() {
        return car;
    }

    public Spot getStart_spot() {
        return start_spot;
    }

    public Spot getEnd_spot() {
        return end_spot;
    }
}
