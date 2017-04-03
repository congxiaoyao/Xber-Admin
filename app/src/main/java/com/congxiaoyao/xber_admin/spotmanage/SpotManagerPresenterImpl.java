package com.congxiaoyao.xber_admin.spotmanage;

import android.widget.Toast;

import com.congxiaoyao.httplib.request.SpotRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by guo on 2017/3/24.
 */

public class SpotManagerPresenterImpl extends ListLoadablePresenterImpl<SpotManagerContract.View>
        implements SpotManagerContract.Presenter {

    public SpotManagerPresenterImpl(SpotManagerContract.View view) {
        super(view);
    }

    @Override
    public Observable<? extends List> pullListData() {
        Observable<List<Spot>> observable = XberRetrofit.create(SpotRequest.class)
                .getAllSpots(Token.value)
                .compose(RxUtils.<List<Spot>>delayWhenTimeEnough(300));
        return observable;
    }

    @Override
    public void remove(Spot spot) {
        view.showLoading();
        XberRetrofit.create(SpotRequest.class)
                .deleteSpot(spot.getSpotId(), Token.value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(view.getContext(), s, Toast.LENGTH_SHORT).show();
                        refreshData();
                    }
                }, this);
    }
}
