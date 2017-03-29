package com.congxiaoyao.xber_admin.spotmanage;

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
import rx.functions.Action1;

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
}
