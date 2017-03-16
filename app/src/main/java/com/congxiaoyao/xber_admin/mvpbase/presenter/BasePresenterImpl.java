package com.congxiaoyao.xber_admin.mvpbase.presenter;

import android.content.Context;

import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;
import com.congxiaoyao.httplib.response.exception.AuthException;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.httplib.response.exception.ExceptionDispatcher;
import com.congxiaoyao.httplib.response.exception.IExceptionHandler;
import com.congxiaoyao.httplib.response.exception.LoginException;
import com.congxiaoyao.httplib.response.exception.NetWorkException;
import com.congxiaoyao.httplib.response.exception.PermissionDeniedException;
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
        view.hideLoading();
        toastExceptionHandler.onDispatchException(throwable);
    }

    @Override
    public void onLoginError(LoginException exception) {
        toastExceptionHandler.onLoginError(exception);
    }

    @Override
    public void onAuthError(AuthException exception) {
        toastExceptionHandler.onAuthError(exception);
    }

    @Override
    public void onPermissionError(PermissionDeniedException exception) {
        toastExceptionHandler.onPermissionError(exception);
    }

    @Override
    public void onEmptyDataError(EmptyDataException exception) {
        toastExceptionHandler.onEmptyDataError(exception);
    }

    @Override
    public boolean onTimeoutError(SocketTimeoutException exception) {
        return toastExceptionHandler.onTimeoutError(exception);
    }

    @Override
    public boolean onUnknowHostError(UnknownHostException exception) {
        return toastExceptionHandler.onUnknowHostError(exception);
    }

    @Override
    public void onNetworkError(NetWorkException exception) {
        toastExceptionHandler.onNetworkError(exception);
    }

    @Override
    public void unKnowError(Throwable throwable) {
        toastExceptionHandler.unKnowError(throwable);
    }
}