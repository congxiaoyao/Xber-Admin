package com.congxiaoyao.xber_admin.driverslist;

import android.util.Log;

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

public class DriverItemPresenterImpl  extends PagedListLoadablePresenterImpl<DriverItemContract.View>
        implements DriverItemContract.Presenter{

    public static final int PAGE_SIZE = 10;

    public DriverItemPresenterImpl(DriverItemContract.View view) {
        super(view);
    }

    @Override
    public  Observable pullPagedListData(final int page) {
        final TaskRequest taskRequest = XberRetrofit.create(TaskRequest.class);
        if (page!=0) {
            return taskRequest
                    .getTask(((DriverItemActivity) view.getContext()).getParcel().getUserInfo().getUserId()
                            , page
                            , PAGE_SIZE
                            , Task.STATUS_COMPLETED
                            , timeStamp.getTime(), null, Token.value);
        }
        Observable observable = taskRequest
                .getTask(((DriverItemActivity) view.getContext()).getParcel().getUserInfo().getUserId()
                        , page
                        , PAGE_SIZE
                        , Task.STATUS_EXECUTING
                        , System.currentTimeMillis()
                        , null
                        , Token.value).flatMap(new Func1<TaskListRsp, Observable<?>>() {
                    @Override
                    public Observable<?> call(TaskListRsp taskListRsp) {
                        Log.d("gdy", "call: "+taskListRsp.getTaskList());
                        if (taskListRsp.getTaskList().size()>0){
                            view.addExecutingTask(taskListRsp.getTaskList().get(0));
                        }
                        return taskRequest.getTask(((DriverItemActivity) view.getContext()).getParcel().getUserInfo().getUserId()
                                ,page
                                ,PAGE_SIZE
                                ,Task.STATUS_COMPLETED
                                ,System.currentTimeMillis()
                                ,null
                                ,Token.value);
                    }
                });
        return observable;
    }
}
