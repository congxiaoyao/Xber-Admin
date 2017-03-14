package com.congxiaoyao.xber_admin.mvpbase.presenter;

import android.content.Context;

import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;
import com.congxiaoyao.httplib.response.exception.ExceptionDispatcher;
import com.congxiaoyao.httplib.response.exception.IExceptionHandler;
import com.congxiaoyao.httplib.response.exception.ResponseException;
import com.congxiaoyao.httplib.response.exception.StatusException;
import com.congxiaoyao.xber_admin.ToastAndLogExceptionHandler;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import rx.subscriptions.CompositeSubscription;

/**
 * 为子类提供默认的错误处理的功能 并直接使得子类拥有错误处理的能力
 * 由于类内成员变量{@link BasePresenterImpl#exceptionDispatcher}提供分发错误的功能
 * 所以只要子类通过这个对象的{@link ExceptionDispatcher#dispatchException(Throwable throwable)}方法
 * 分发错误 即可在不同的方法中处理不同的错误
 *
 * 除了错误处理 作为{@link BasePresenter}的实现类 完成了mvp的流程 实现了依赖的注入
 * 同时也实现了 {@link BasePresenter#unSubscribe()}方法
 * 注意 这里是空实现的{@link BasePresenter#subscribe()} 所以子类不要忘了覆写
 *
 * Created by congxiaoyao on 2016/8/25.
 */
public class BasePresenterImpl<T extends LoadableView> implements BasePresenter,
        IExceptionHandler {

    protected T view;
    protected CompositeSubscription subscriptions;
    protected ExceptionDispatcher exceptionDispatcher;
    protected IExceptionHandler toastExceptionHandler;

    public BasePresenterImpl(final T view) {
        this.view = view;
        view.setPresenter(this);
        subscriptions = new CompositeSubscription();
        exceptionDispatcher = new ExceptionDispatcher();
        exceptionDispatcher.setExceptionHandler(this);
        toastExceptionHandler = new ToastAndLogExceptionHandler(
                new ToastAndLogExceptionHandler.ContextProvider() {
                    @Override
                    public Context getContext() {
                        return view.getContext();
                    }
                });
    }

    @Override
    public void subscribe() {
        throw new RuntimeException("请覆写此方法订阅数据");
    }

    @Override
    public void unSubscribe() {
        subscriptions.unsubscribe();
    }

    /**
     * 错误已经发生 但还没有分发事件时候的回调
     * @param throwable
     */
    @Override
    public void onDispatchException(Throwable throwable) {
        toastExceptionHandler.onDispatchException(throwable);
    }

    /**
     * 所有的ResponseException的统一错误处理 如果发生了一个ResponseException
     * 在不覆写任何错误处理函数的情况下 一定会走到这里
     * @param exception
     */
    @Override
    public void onResponseError(ResponseException exception) {
        toastExceptionHandler.onResponseError(exception);
    }

    @Override
    public void onTimeoutError(SocketTimeoutException exception) {
        toastExceptionHandler.onTimeoutError(exception);
    }

    @Override
    public void onUnknowHostError(UnknownHostException exception) {
        toastExceptionHandler.onUnknowHostError(exception);
    }

    @Override
    public void onHttpError(HttpException exception) {
        toastExceptionHandler.onHttpError(exception);
    }

    @Override
    public boolean onEmptyDataError(ResponseException exception) {
        return toastExceptionHandler.onEmptyDataError(exception);
    }

    /**
     * 状态码错误 上层为{@link IExceptionHandler#onResponseError(ResponseException)}
     * @param exception
     * @return
     */
    @Override
    public boolean onStatusError(StatusException exception) {
        return toastExceptionHandler.onStatusError(exception);
    }

    /**
     * 网络连接不可用 上层为{@link IExceptionHandler#onResponseError(ResponseException)}
     * @param msg
     * @return
     */
    @Override
    public boolean onNullNetworkError(String msg) {
        return toastExceptionHandler.onNullNetworkError(msg);
    }

    /**
     * 非{@link ResponseException}的其他类型的Throwable
     * @param throwable
     * @return
     */
    @Override
    public boolean unKnowError(Throwable throwable) {
        return toastExceptionHandler.unKnowError(throwable);
    }

    /**
     * 未登录 上层为{@link IExceptionHandler#onStatusError(StatusException)}
     * @param reason
     * @return
     */
    @Override
    public boolean onUnLogin(String reason) {
        return toastExceptionHandler.onUnLogin(reason);
    }

    /**
     * 登录错误 上层为{@link IExceptionHandler#onStatusError(StatusException)}
     * @param reason
     * @return
     */
    @Override
    public boolean onLoginError(String reason) {
        return toastExceptionHandler.onLoginError(reason);
    }
}