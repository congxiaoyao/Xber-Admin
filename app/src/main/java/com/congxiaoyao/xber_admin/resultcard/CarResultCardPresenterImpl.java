package com.congxiaoyao.xber_admin.resultcard;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Car;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.service.StompService;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class CarResultCardPresenterImpl extends ListLoadablePresenterImpl<CarResultCardContract.View>
        implements CarResultCardContract.Presenter {

    private CarResultCardContract.OnCarSelectedListener listener;
    private boolean isSearchPlate = true;
    private String content = "";

    public CarResultCardPresenterImpl(CarResultCardContract.View view) {
        super(view);
    }

    @Override
    public Observable<? extends List> pullListData() {
        if (content.equals("")) return null;
        if (isSearchPlate) {
            return searchPlate();
        } else return searchName();
    }

    private Observable<? extends List> searchName() {
        final long pre = System.currentTimeMillis();
        return XberRetrofit.create(CarRequest.class).getCarsByName(content, Token.value)
                .doOnNext(new Action1<List<CarDetail>>() {
                    @Override
                    public void call(final List<CarDetail> carDetails) {
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
                                view.requestResize(carDetails.size());
                            }
                        });
                    }
                });
    }

    private Observable<? extends List> searchPlate() {
        final long pre = System.currentTimeMillis();
        return XberRetrofit.create(CarRequest.class).getCarsByPlate(content, Token.value)
                .doOnNext(new Action1<List<CarDetail>>() {
                    @Override
                    public void call(final List<CarDetail> carDetails) {
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
                                view.requestResize(carDetails.size());
                            }
                        });
                    }
                });
    }

    @Override
    public void setOnCarSelectedListener(CarResultCardContract.OnCarSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void search(String content) {
        if(content == null || content.equals("")) {
            this.content = "";
            return;
        }

        isSearchPlate = isContentNumber(content);
        this.content = content;
        subscribe();
    }

    @Override
    public void callClick(CarDetail carDetail) {
        if (listener != null) {
            listener.onCarSelected(carDetail);
        }
    }

    private static boolean isContentNumber(String value) {
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= '0' && c <= '9') {
                return true;
            }
        }
        return false;
    }
}
