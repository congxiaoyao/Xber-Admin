package com.congxiaoyao.xber_admin.resultcard;

import android.util.SparseArray;
import android.util.SparseLongArray;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.LocationRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Car;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.CarPosition;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.service.StompService;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class CarResultCardPresenterImpl extends ListLoadablePresenterImpl<CarResultCardContract.View>
        implements CarResultCardContract.Presenter {

    private CarResultCardContract.OnCarSelectedListener listener;
    private boolean isSearchPlate = true;
    private String content = "";
    private List<CarDetail> temp;
    private List<CarPosition> carPositions;

    public CarResultCardPresenterImpl(CarResultCardContract.View view) {
        super(view);
    }

    @Override
    public Observable<? extends List> pullListData() {
        if (content.equals("")) return null;
        Observable<List<CarDetail>> observable = isSearchPlate ? searchPlate() : searchName();
        observable = observable.map(new Func1<List<CarDetail>, List<Long>>() {
            @Override
            public List<Long> call(List<CarDetail> carDetails) {
                temp = carDetails;
                List<Long> carIds = new ArrayList<Long>(carDetails.size());
                for (CarDetail carDetail : carDetails) {
                    carIds.add(carDetail.getCarId());
                }
                return carIds;
            }
        }).flatMap(new Func1<List<Long>, Observable<List<CarPosition>>>() {
            @Override
            public Observable<List<CarPosition>> call(List<Long> longs) {
                return XberRetrofit.create(LocationRequest.class).getRunningCars(longs, Token.value);
            }
        }).map(new Func1<List<CarPosition>, List<CarDetail>>() {
            @Override
            public List<CarDetail> call(List<CarPosition> carPositions) {
                CarResultCardPresenterImpl.this.carPositions = carPositions;
                SparseArray<CarDetail> array = new SparseArray<CarDetail>(temp.size());
                for (CarDetail carDetail : temp) {
                    array.append((int) ((long) carDetail.getCarId()), carDetail);
                }
                List<CarDetail> result = new ArrayList<CarDetail>(carPositions.size());
                for (CarPosition carPosition : carPositions) {
                    CarDetail carDetail = array.get((int) ((long) carPosition.getCarId()));
                    if (carDetail != null) {
                        result.add(carDetail);
                    }
                }
                return result;
            }
        });
        return observable.compose(RxUtils.<List<CarDetail>>delayWhenTimeEnough(300)).doOnNext(new Action1<List<CarDetail>>() {
            @Override
            public void call(final List<CarDetail> carDetails) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.requestResize(carDetails.size());
                    }
                });
            }
        });
    }

    private Observable<List<CarDetail>> searchName() {
        return XberRetrofit.create(CarRequest.class).getCarsByName(content, Token.value);
    }

    private Observable<List<CarDetail>> searchPlate() {
        return XberRetrofit.create(CarRequest.class).getCarsByPlate(content, Token.value);
    }

    @Override
    public void setOnCarSelectedListener(CarResultCardContract.OnCarSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public List<CarPosition> getCarPositions() {
        return carPositions;
    }

    private CarPosition findCarPositionById(Long carId) {
        if (carPositions == null) return null;
        for (CarPosition carPosition : carPositions) {
            if (carPosition.getCarId().equals(carId)) {
                return carPosition;
            }
        }
        return null;
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
            listener.onCarSelected(carDetail,findCarPositionById(carDetail.getCarId()));
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
