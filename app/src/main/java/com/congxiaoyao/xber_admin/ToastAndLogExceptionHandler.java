package com.congxiaoyao.xber_admin;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;
import com.congxiaoyao.httplib.response.exception.AuthException;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.httplib.response.exception.IExceptionHandler;
import com.congxiaoyao.httplib.response.exception.LoginException;
import com.congxiaoyao.httplib.response.exception.NetWorkException;
import com.congxiaoyao.httplib.response.exception.PermissionDeniedException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class ToastAndLogExceptionHandler implements IExceptionHandler {

    private ContextProvider context;

    public ToastAndLogExceptionHandler(ContextProvider context) {
        this.context = context;
    }

    /**
     * 错误已经发生 但还没有分发事件时候的回调
     * @param throwable
     */
    @Override
    public void onDispatchException(Throwable throwable) {

    }

    @Override
    public void onLoginError(LoginException exception) {
        Log.d(TAG.ME, "onLoginError: ", exception);
    }

    @Override
    public void onAuthError(AuthException exception) {
        Log.d(TAG.ME, "onAuthError: ", exception);
    }

    @Override
    public void onPermissionError(PermissionDeniedException exception) {
        Toast.makeText(context.getContext(), "没有访问权限", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyDataError(EmptyDataException exception) {
        Toast.makeText(context.getContext(), "暂时没有数据", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTimeoutError(SocketTimeoutException exception) {
        Log.d(TAG.ME, "onTimeoutError: 网络超时");
        return false;
    }


    @Override
    public boolean onUnknowHostError(UnknownHostException exception) {
        Log.d(TAG.ME, "onUnknowHostError: 域名解析失败 请检查网络连接");
        return false;
    }

    /**
     * 如果上面两个方法返回了false 则此方法将会继续被调用
     * @param exception
     * @see ToastAndLogExceptionHandler#onUnknowHostError(UnknownHostException)
     * @see ToastAndLogExceptionHandler#onTimeoutError(SocketTimeoutException)
     */
    @Override
    public void onNetworkError(NetWorkException exception) {
        Toast.makeText(context.getContext(), "请检查网络连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unKnowError(Throwable throwable) {
        Toast.makeText(context.getContext(), "未知错误", Toast.LENGTH_SHORT).show();
    }

    public interface ContextProvider {

        Context getContext();
    }
}
