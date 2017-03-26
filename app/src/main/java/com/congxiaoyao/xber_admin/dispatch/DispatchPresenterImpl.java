package com.congxiaoyao.xber_admin.dispatch;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Car;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Page;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by guo on 2017/3/16.
 */

public class DispatchPresenterImpl extends ListLoadablePresenterImpl<DispatchContract.View>
        implements DispatchContract.Presenter{

    public DispatchPresenterImpl(DispatchContract.View view) {
        super(view);
    }

    @Override
    public Observable<? extends List> pullListData() {
        Observable<List<CarDetail>> observable = XberRetrofit.create(CarRequest.class)
                .getFreeCars(view.getParent().getData().getStartTime(),
                        view.getParent().getData().getEndTime(), Token.value)
                .compose(RxUtils.<List<CarDetail>>delayWhenTimeEnough(300))
                .doOnNext(new Action1<List<CarDetail>>() {
                    @Override
                    public void call(List<CarDetail> carDetails) {
                        view.clear();
                    }
                });
        return observable;
    }

    @Override
    public void setCar(CarDetail car) {
        ((DispatchTaskActivity) view.getContext()).setCarId(car.getCarId());
        ((DispatchTaskActivity) view.getContext()).setCar(car);
    }
}
