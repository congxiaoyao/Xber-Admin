package com.congxiaoyao.xber_admin.monitoring.carinfo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.LocationRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Car;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.CarPosition;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.xber_admin.MainActivity;
import com.congxiaoyao.xber_admin.driverslist.DriverListActivity;
import com.congxiaoyao.xber_admin.driverslist.DriverListFragment;
import com.congxiaoyao.xber_admin.driverslist.module.CarDetailParcel;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.publishedtask.TaskTrackActivity;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.congxiaoyao.xber_admin.helpers.TopSearchBar.carPositionToLatLngBounds;

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
                .compose(RxUtils.<CarDetail>delayWhenTimeEnough(300))
                .compose(RxUtils.<CarDetail>defaultScheduler())
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
            Intent intent = new Intent(view.getContext(), TaskTrackActivity.class);
            intent.putExtra(DriverListActivity.EXTRA_CARDETIAL, parcel);
            view.getContext().startActivity(intent);
        }
        view.hideContentView();
    }

    @Override
    public void onShowLocation(View button) {
        view.showLoading();
        moveMapToCarPosition();
    }

    protected void moveMapToCarPosition() {
        Subscription subscribe = XberRetrofit.create(LocationRequest.class)
                .getRunningCars(Arrays.asList(carId), Token.value)
                .flatMap(new Func1<List<CarPosition>, Observable<CarPosition>>() {
                    @Override
                    public Observable<CarPosition> call(List<CarPosition> carPositions) {
                        return Observable.from(carPositions);
                    }
                })
                .take(1)
                .map(new Func1<CarPosition, LatLngBounds>() {
                    @Override
                    public LatLngBounds call(CarPosition carPosition) {
                        return carPositionToLatLngBounds(carPosition);
                    }
                })
                .compose(RxUtils.<LatLngBounds>defaultScheduler())
                .subscribe(new Action1<LatLngBounds>() {
                    @Override
                    public void call(LatLngBounds latLngBounds) {
                        view.hideLoading();
                        view.hideContentView();
                        MainActivity.moveMap(view.getContext(), latLngBounds);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        exceptionDispatcher.dispatchException(throwable);
                    }
                });
        subscriptions.add(subscribe);
    }
}
