package com.congxiaoyao.xber_admin.resultcard;

import android.app.Activity;
import android.view.View;

import com.congxiaoyao.httplib.request.SpotRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.helpers.SearchAddrBar;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class AddrResultCardPresenterImpl extends ListLoadablePresenterImpl<AddrResultCardContract.View>
        implements AddrResultCardContract.Presenter{

    private final SearchAddrBar searchAddrBar;
    private boolean isSelectingStart = true;

    public AddrResultCardPresenterImpl(AddrResultCardContract.View view, SearchAddrBar searchAddrBar) {
        super(view);
        this.searchAddrBar = searchAddrBar;
    }

    @Override
    public Observable<? extends List> pullListData() {
        View root = searchAddrBar.getBinding().getRoot();
        return XberRetrofit.create(SpotRequest.class)
                .getAllSpots(Token.value)
                .compose(RxUtils.<List<Spot>>delayWhenTimeEnough(300))
                .compose(RxUtils.post(root, new Action1<List<Spot>>() {
                    @Override
                    public void call(List<Spot> spots) {
                        view.requestResize(spots.size());
                    }
                }));
    }

    @Override
    public void selectStart() {
        isSelectingStart = true;
        view.setLocationIcon(R.drawable.icon_location_start);
        subscribe();
    }

    @Override
    public void selectEnd() {
        isSelectingStart = false;
        view.setLocationIcon(R.drawable.icon_location_goal);
        subscribe();
    }

    @Override
    public void onSelectSpot(Spot spot) {
        if (isSelectingStart) {
            searchAddrBar.setStartLocation(spot);
        }else {
            searchAddrBar.setEndLocation(spot);
        }
        view.hideMyself(new Action0() {
            @Override
            public void call() {
                searchAddrBar.removeLocationSelectCard();
            }
        });
    }
}
