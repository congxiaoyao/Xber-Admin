package com.congxiaoyao.xber_admin.monitoring.carinfo;

import android.content.Intent;
import android.view.View;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.driverslist.DriverListActivity;
import com.congxiaoyao.xber_admin.driverslist.DriverListFragment;
import com.congxiaoyao.xber_admin.driverslist.driverdetail.DriverDetailActivity;
import com.congxiaoyao.xber_admin.driverslist.module.CarDetailParcel;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/4/25.
 */

public class CarInfoPresenter extends BasePresenterImpl<CarInfoView>
        implements CarInfoContract.Presenter {

    private CarDetail carDetail;
    private final Long carId;

    public CarInfoPresenter(CarInfoView view, Long carId) {
        super(view);
        this.carId = carId;
        view.showContentView();
    }

    @Override
    public void subscribe() {
        view.showLoading();
        Subscription subscribe = XberRetrofit.create(CarRequest.class)
                .getCarInfo(carId, Token.value)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CarDetail>() {
                    @Override
                    public void call(CarDetail carDetail) {
                        CarInfoPresenter.this.carDetail = carDetail;
                        view.hideLoading();
                        view.bindData(carDetail);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        exceptionDispatcher.dispatchException(throwable);
                    }
                });
        subscriptions.add(subscribe);
    }

    @Override
    public void onDispatchException(Throwable throwable) {
        super.onDispatchException(throwable);
        view.hideContentView();
    }

    @Override
    public void onClick(View button) {
        if (carDetail != null) {
            CarDetailParcel parcel = DriverListFragment.carDetailToParcel(carDetail);
            Intent intent = new Intent(view.getContext(), DriverDetailActivity.class);
            intent.putExtra(DriverListActivity.EXTRA_CARDETIAL, parcel);
            view.getContext().startActivity(intent);
        }
        view.hideContentView();
    }
}
