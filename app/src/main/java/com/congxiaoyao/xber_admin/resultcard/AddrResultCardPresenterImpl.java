package com.congxiaoyao.xber_admin.resultcard;

import com.congxiaoyao.httplib.request.SpotRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.helpers.SearchAddrBar;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
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
        final long pre = System.currentTimeMillis();
        return XberRetrofit.create(SpotRequest.class)
                .getAllSpots(Token.value).doOnNext(new Action1<List<Spot>>() {
                    @Override
                    public void call(final List<Spot> spots) {
                        int minTime = 400;
                        if (System.currentTimeMillis() - pre < minTime) {
                            try {
                                Thread.sleep(minTime - System.currentTimeMillis() + pre);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.requestResize(spots.size());
                            }
                        });
                    }
                });
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
