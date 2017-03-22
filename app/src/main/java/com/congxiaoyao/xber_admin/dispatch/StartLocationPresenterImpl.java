package com.congxiaoyao.xber_admin.dispatch;

import com.congxiaoyao.httplib.request.SpotRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by guo on 2017/3/22.
 */

public class StartLocationPresenterImpl extends ListLoadablePresenterImpl<StratLocationContract.View>
        implements StratLocationContract.Presenter{

    public StartLocationPresenterImpl(StratLocationContract.View view) {
        super(view);
    }

    @Override
    public Observable<? extends List> pullListData() {
        Observable<List<Spot>> observable = XberRetrofit.create(SpotRequest.class)
                .getAllSpots(Token.value)
                .doOnNext(new Action1<List<Spot>>() {
                    @Override
                    public void call(List<Spot> spots) {
                        view.clear();
                    }
                });
        return observable;
    }
}
