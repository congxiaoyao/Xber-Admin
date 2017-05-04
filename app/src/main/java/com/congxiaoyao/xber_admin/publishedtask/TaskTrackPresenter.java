package com.congxiaoyao.xber_admin.publishedtask;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskListRsp;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.xber_admin.driverslist.DriverListActivity;
import com.congxiaoyao.xber_admin.driverslist.module.CarDetailParcel;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskRspAndDriver;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskTrackContact;
import com.congxiaoyao.xber_admin.spotmanage.ParcelSpot;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageHelper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static com.congxiaoyao.xber_admin.driverslist.DriverListFragment.carDetailToParcel;

/**
 * Created by congxiaoyao on 2017/4/30.
 */

public class TaskTrackPresenter extends BasePresenterImpl<TaskTrackContact.View>
        implements TaskTrackContact.Presenter, Action1<Throwable> {


    private static final String KEY_TASK_ID = "KEY_TASK_ID";
    private static final String KEY_CAR_ID = "KEY_CAR_ID";
    public static final String KEY_TASK_STATUS = "KEY_TASK_STATUS";

    public TaskTrackPresenter(TaskTrackContact.View view) {
        super(view);
    }

    @Override
    public void subscribe() {
        Intent intent = ((Activity) view.getContext()).getIntent();
        Parcelable extra = intent.getParcelableExtra(PublishedTaskListFragment.KEY_TASK);
        if (extra != null) {
            TaskRspAndDriver task = (TaskRspAndDriver) extra;
            view.showTask(task);
            return;
        }
        Serializable serializable = intent.getSerializableExtra(PushMessageHelper.KEY_MESSAGE);
        if (serializable == null) {
            CarDetailParcel carDetail = intent
                    .getParcelableExtra(DriverListActivity.EXTRA_CARDETIAL);
            if (carDetail != null) {
                subscribeByCarId(carDetail);
            } else {
                view.showError();
            }
            return;
        }
        long taskId = -1;
        try {
            MiPushMessage message = (MiPushMessage) serializable;
            taskId = Long.parseLong(message.getExtra().get(KEY_TASK_ID));
        } catch (Exception e) {
            view.showError();
            return;
        }
        view.showLoading();
        Subscription subscription = XberRetrofit.create(TaskRequest.class)
                .getTask(String.valueOf(taskId), Token.value).flatMap(new Func1<TaskRsp, Observable<TaskRspAndDriver>>() {
                    @Override
                    public Observable<TaskRspAndDriver> call(TaskRsp taskRsp) {
                        return Observable.zip(Observable.just(taskRsp),
                                XberRetrofit.create(CarRequest.class)
                                        .getCarInfo(taskRsp.getCarId(), Token.value), new Func2<TaskRsp, CarDetail, TaskRspAndDriver>() {
                                    @Override
                                    public TaskRspAndDriver call(TaskRsp taskRsp, CarDetail carDetail) {
                                        return merge(taskRsp, carDetail);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<TaskRspAndDriver>() {
                    @Override
                    public void call(TaskRspAndDriver taskRspAndDriver) {
                        view.hideLoading();
                        view.showTask(taskRspAndDriver);
                    }
                }, TaskTrackPresenter.this);

        subscriptions.add(subscription);
    }

    private void subscribeByCarId(final CarDetailParcel carDetail) {
        Subscription subscribe = XberRetrofit.create(TaskRequest.class).getTask(carDetail.getUserInfo().getUserId(),
                0, 1, Task.STATUS_EXECUTING, System.currentTimeMillis(), null, Token.value)
                .flatMap(new Func1<TaskListRsp, Observable<TaskListRsp>>() {
                    @Override
                    public Observable<TaskListRsp> call(TaskListRsp taskListRsp) {
                        List<TaskRsp> pageData = taskListRsp.getCurrentPageData();
                        if (pageData == null || pageData.size() == 0) {
                            return XberRetrofit.create(TaskRequest.class).getTask(carDetail.getUserInfo().getUserId(),
                                    0, 1, Task.STATUS_DELIVERED, System.currentTimeMillis(), null, Token.value);
                        }
                        return Observable.just(taskListRsp);
                    }
                })
                .compose(RxUtils.<TaskListRsp>defaultScheduler())
                .subscribe(new Action1<TaskListRsp>() {
                    @Override
                    public void call(TaskListRsp taskListRsp) {
                        List<TaskRsp> pageData = taskListRsp.getCurrentPageData();
                        if (pageData == null || pageData.size() == 0) {
                            throw new EmptyDataException();
                        }
                        TaskRspAndDriver taskRsp = getTaskRspWithoutDriver(pageData.get(0));
                        taskRsp.setCarDetail(carDetail);
                        view.hideLoading();
                        view.showTask(taskRsp);
                    }
                }, this);

        subscriptions.add(subscribe);
    }

    private TaskRspAndDriver merge(TaskRsp taskRsp, CarDetail carDetail) {
        TaskRspAndDriver taskRspAndDriver = getTaskRspWithoutDriver(taskRsp);
        taskRspAndDriver.setCarDetail(carDetailToParcel(carDetail));
        return taskRspAndDriver;
    }

    @NonNull
    public static TaskRspAndDriver getTaskRspWithoutDriver(TaskRsp taskRsp) {
        TaskRspAndDriver taskRspAndDriver = new TaskRspAndDriver();
        taskRspAndDriver.setCarId(taskRsp.getCarId());
        taskRspAndDriver.setTaskId(taskRsp.getTaskId());
        taskRspAndDriver.setStartTime(dateToTime(taskRsp.getStartTime()));
        taskRspAndDriver.setStartSpot(toParcelSpot(taskRsp.getStartSpot()));
        taskRspAndDriver.setEndTime(dateToTime(taskRsp.getEndTime()));
        taskRspAndDriver.setEndSpot(toParcelSpot(taskRsp.getEndSpot()));
        taskRspAndDriver.setContent(taskRsp.getContent());
        taskRspAndDriver.setCreateUser(taskRsp.getCreateUser());
        taskRspAndDriver.setCreateTime(dateToTime(taskRsp.getCreateTime()));
        taskRspAndDriver.setRealStartTime(dateToTime(taskRsp.getRealStartTime()));
        taskRspAndDriver.setRealEndTime(dateToTime(taskRsp.getRealEndTime()));
        taskRspAndDriver.setStatus(taskRsp.getStatus());
        taskRspAndDriver.setNote(taskRsp.getNote());
        return taskRspAndDriver;
    }

    public static Long dateToTime(Date date) {
        if (date == null) return null;
        return date.getTime();
    }

    public static ParcelSpot toParcelSpot(Spot spot) {
        ParcelSpot parcelSpot = new ParcelSpot(spot.getSpotId(),
                spot.getSpotName(), spot.getLatitude(), spot.getLongitude());
        return parcelSpot;
    }

    @Override
    public void call(Throwable throwable) {
        exceptionDispatcher.dispatchException(throwable);
    }
}
