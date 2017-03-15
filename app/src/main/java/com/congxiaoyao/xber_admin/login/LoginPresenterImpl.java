package com.congxiaoyao.xber_admin.login;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.util.Log;

import com.congxiaoyao.Admin;
import com.congxiaoyao.httplib.request.LoginRequest;
import com.congxiaoyao.httplib.request.body.LoginBody;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.LoginInfoResponse;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public class LoginPresenterImpl extends BasePresenterImpl<LoginContract.View>
        implements LoginContract.Presenter{

    String userName = "";
    String password = "";

    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
    }

    @Override
    public Admin getAdmin() {
        return Admin.fromSharedPreference(view.getContext());
    }

    @Override
    public void login(String userName, String password) {
        this.userName = userName;
        this.password = password;
        subscribe();
    }

    @Override
    public void subscribe() {
        LoginBody body = new LoginBody();
        body.setClientId(Build.SERIAL);
        Log.d(TAG.ME, "clientId = " + body.getClientId());
        body.setUsername(userName);
        body.setPassword(password);
        view.showLoading();
        XberRetrofit.create(LoginRequest.class).login(body).doOnNext(new Action1<LoginInfoResponse>() {
            @Override
            public void call(LoginInfoResponse loginInfoResponse) {
                Admin admin = new Admin();
                admin.setPassword(password);
                admin.setNickName(loginInfoResponse.getName());
                admin.setUserName(loginInfoResponse.getUsername());
                Token.processTokenAndSave(loginInfoResponse.getAuthToken());
                admin.setToken(Token.value);
                admin.save(view.getContext());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<LoginInfoResponse>() {
            @Override
            public void call(LoginInfoResponse loginInfoResponse) {
                view.showLoginSuccess();
                ((Activity) view.getContext()).finish();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                exceptionDispatcher.dispatchException(throwable);
            }
        });
    }
}
