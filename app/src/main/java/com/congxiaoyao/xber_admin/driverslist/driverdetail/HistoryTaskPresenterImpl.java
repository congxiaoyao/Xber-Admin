package com.congxiaoyao.xber_admin.driverslist.driverdetail;

import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskListRsp;
import com.congxiaoyao.xber_admin.mvpbase.presenter.PagedListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by guo on 2017/3/29.
 */

public class HistoryTaskPresenterImpl extends PagedListLoadablePresenterImpl<HistoryTaskContract.View>
        implements HistoryTaskContract.Presenter{

    public static final int PAGE_SIZE = 10;

    public HistoryTaskPresenterImpl(HistoryTaskContract.View view) {
        super(view);
    }

    @Override
    public  Observable<TaskListRsp> pullPagedListData(final int page) {
        final TaskRequest taskRequest = XberRetrofit.create(TaskRequest.class);
        if (page > 0)
            return taskRequest.getTask(view.getDriverId(), page, PAGE_SIZE,
                    Task.STATUS_COMPLETED, timeStamp == null ?
                            System.currentTimeMillis() : timeStamp.getTime(), null, Token.value);

        return taskRequest.getTask(view.getDriverId(),
                0, PAGE_SIZE, Task.STATUS_EXECUTING,
                System.currentTimeMillis(), null, Token.value)
                .flatMap(new Func1<TaskListRsp, Observable<TaskListRsp>>() {
                    @Override
                    public Observable<TaskListRsp> call(TaskListRsp taskListRsp) {
                        if (taskListRsp.getTaskList().size() > 0)
                            view.addExecutingTask(taskListRsp.getTaskList().get(0));
                        return taskRequest.getTask(view.getDriverId(),
                                0, PAGE_SIZE,
                                Task.STATUS_COMPLETED,
                                System.currentTimeMillis(), null, Token.value);
                    }
                });
    }

    @Override
    public void refreshData() {
        view.clearHeader();
        view.showNothing();
        super.refreshData();
    }
}
