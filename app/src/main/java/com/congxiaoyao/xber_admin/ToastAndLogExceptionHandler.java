package com.congxiaoyao.xber_admin;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;
import com.congxiaoyao.httplib.response.exception.IExceptionHandler;
import com.congxiaoyao.httplib.response.exception.ResponseException;
import com.congxiaoyao.httplib.response.exception.StatusException;

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

    /**
     * 所有的ResponseException的统一错误处理 如果发生了一个ResponseException
     * 在不覆写任何错误处理函数的情况下 一定会走到这里
     * @param exception
     */
    @Override
    public void onResponseError(ResponseException exception) {
        Log.d(TAG.ME, "ExceptionHandler onResponseError: " + exception);
    }

    @Override
    public void onTimeoutError(SocketTimeoutException exception) {
        Toast.makeText(context.getContext(), "网络超时", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnknowHostError(UnknownHostException exception) {
        Toast.makeText(context.getContext(), "域名解析失败 请检查网络连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHttpError(HttpException exception) {
        Toast.makeText(context.getContext(), "服务不可达", Toast.LENGTH_SHORT).show();
        Log.d(TAG.ME, "onHttpError: " + exception);
    }

    @Override
    public boolean onEmptyDataError(ResponseException exception) {
        Toast.makeText(context.getContext(), "暂时没有数据", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * 状态码错误 上层为{@link IExceptionHandler#onResponseError(ResponseException)}
     * @param exception
     * @return
     */
    @Override
    public boolean onStatusError(StatusException exception) {
        Log.d(TAG.ME, "ExceptionHandler onStatusError: " + exception);
        return false;
    }

    /**
     * 网络连接不可用 上层为{@link IExceptionHandler#onResponseError(ResponseException)}
     * @param msg
     * @return
     */
    @Override
    public boolean onNullNetworkError(String msg) {
        Toast.makeText(context.getContext(), "网络连接不可用", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 非{@link ResponseException}的其他类型的Throwable
     * @param throwable
     * @return
     */
    @Override
    public boolean unKnowError(Throwable throwable) {
        throwable.printStackTrace();
        return false;
    }

    /**
     * 未登录 上层为{@link IExceptionHandler#onStatusError(StatusException)}
     * @param reason
     * @return
     */
    @Override
    public boolean onUnLogin(String reason) {
        Log.d(TAG.ME, "ExceptionHandler onUnLogin: " + reason);
        return false;
    }

    /**
     * 登录错误 上层为{@link IExceptionHandler#onStatusError(StatusException)}
     * @param reason
     * @return
     */
    @Override
    public boolean onLoginError(String reason) {
        Log.d(TAG.ME, "ExceptionHandler onLoginError: " + reason);
        return false;
    }

    public interface ContextProvider {

        Context getContext();
    }
}
