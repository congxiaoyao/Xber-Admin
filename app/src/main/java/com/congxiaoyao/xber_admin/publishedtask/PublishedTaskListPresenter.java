package com.congxiaoyao.xber_admin.publishedtask;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.httplib.response.TaskListRsp;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.httplib.response.exception.LoginException;
import com.congxiaoyao.xber_admin.mvpbase.presenter.PagedListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskRspAndDriver;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskAndDriverListRsp;
import com.congxiaoyao.xber_admin.spotmanage.ParcelSpot;
import com.congxiaoyao.xber_admin.utils.Token;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class PublishedTaskListPresenter extends PagedListLoadablePresenterImpl<PublishedTaskContract.View>
        implements PublishedTaskContract.Presenter {

    public static final int PAGE_SIZE = 10;

    private long userId = -1;
    private int requestType = -1;

    public PublishedTaskListPresenter(PublishedTaskContract.View view) {
        super(view);
    }

    @Override
    public Observable<TaskAndDriverListRsp> pullPagedListData(int page) {
        if (userId == -1 || requestType == -1) {
            exceptionDispatcher.dispatchException(new LoginException("未登录"));
            return null;
        }
        return XberRetrofit.create(TaskRequest.class)
                .getTask(null, page, PAGE_SIZE, requestType,
                        timeStamp == null ? System.currentTimeMillis() : timeStamp.getTime(),
                        userId, Token.value)
                .flatMap(new Func1<TaskListRsp, Observable<TaskAndDriverListRsp>>() {
                    @Override
                    public Observable<TaskAndDriverListRsp> call(TaskListRsp taskListRsp) {
                        return flatMap(taskListRsp);
                    }
                });
    }

    public Observable<TaskAndDriverListRsp> flatMap(final TaskListRsp taskListRsp) {
        return Observable.create(new Observable.OnSubscribe<TaskAndDriverListRsp>() {
            @Override
            public void call(final Subscriber<? super TaskAndDriverListRsp> subscriber) {
                List<TaskRsp> pageData = taskListRsp.getCurrentPageData();
                Set<Long> carIds = new TreeSet<>();
                for (TaskRsp taskRsp : pageData) {
                    carIds.add(taskRsp.getCarId());
                }
                Subscription subscription = Observable.from(carIds).flatMap(new Func1<Long, Observable<CarDetail>>() {
                    @Override
                    public Observable<CarDetail> call(Long aLong) {
                        return XberRetrofit.create(CarRequest.class)
                                .getCarInfo(aLong, Token.value);
                    }
                }).toList().subscribe(new Action1<List<CarDetail>>() {
                    @Override
                    public void call(List<CarDetail> carDetails) {
                        TaskAndDriverListRsp rsp = createEmptyRsp(taskListRsp);
                        insertCarDetailsToRsp(carDetails, rsp);
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    }
                }, PublishedTaskListPresenter.this);
                subscriptions.add(subscription);
            }
        });
    }

    private void insertCarDetailsToRsp(List<CarDetail> carDetails, TaskAndDriverListRsp rsp) {
        List<TaskRspAndDriver> list = rsp.getList();
        for (CarDetail carDetail : carDetails) {
            for (TaskRspAndDriver taskRspAndDriver : list) {
                if (carDetail.getCarId().equals(taskRspAndDriver.getCarId())) {
                    taskRspAndDriver.setCarDetail(carDetail);
                }
            }
        }
    }

    public TaskAndDriverListRsp createEmptyRsp(TaskListRsp taskListRsp) {
        TaskAndDriverListRsp rsp = new TaskAndDriverListRsp();
        rsp.setPage(taskListRsp.getPage());
        rsp.setTimestamp(taskListRsp.getTimestamp());
        List<TaskRspAndDriver> list = new ArrayList<>(taskListRsp.getTaskList().size());
        for (TaskRsp taskRsp : taskListRsp.getTaskList()) {
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
            list.add(taskRspAndDriver);
        }
        rsp.setList(list);
        return rsp;
    }

    public Long dateToTime(Date date) {
        if (date == null) return null;
        return date.getTime();
    }

    public ParcelSpot toParcelSpot(Spot spot) {
        ParcelSpot parcelSpot = new ParcelSpot(spot.getSpotId(),
                spot.getSpotName(), spot.getLatitude(), spot.getLongitude());
        return parcelSpot;
    }

    public PublishedTaskListPresenter setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public PublishedTaskListPresenter setRequestType(int type) {
        this.requestType = type;
        return this;
    }
}
