package com.congxiaoyao.xber_admin.login;

import android.app.Activity;

import com.congxiaoyao.xber_admin.Admin;
import com.congxiaoyao.httplib.request.LoginRequest;
import com.congxiaoyao.httplib.request.body.LoginBody;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.LoginInfoResponse;
import com.congxiaoyao.httplib.response.exception.LoginException;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;
import com.xiaomi.mipush.sdk.MiPushClient;

import rx.Subscription;
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
    public void setLoginResult(int tag) {
        if (view.getContext() instanceof Activity) {
            Activity activity = (Activity) view.getContext();
            activity.setResult(tag);
        }
    }

    @Override
    public void subscribe() {
        LoginBody body = new LoginBody();
        body.setClientId(Token.getClientId(view.getContext()));
        body.setUsername(userName);
        body.setPassword(password);
        view.showLoading();
        Subscription subscribe = XberRetrofit.create(LoginRequest.class).login(body).doOnNext(new Action1<LoginInfoResponse>() {
            @Override
            public void call(LoginInfoResponse loginInfoResponse) {
                Admin admin = new Admin();
                admin.setPassword(password);
                admin.setNickName(loginInfoResponse.getName());
                admin.setUserName(loginInfoResponse.getUsername());
                Token.processTokenAndSave(view.getContext(), loginInfoResponse.getAuthToken());
                admin.setUserId(loginInfoResponse.getUserId());
                admin.setToken(Token.value);
                MiPushClient.setUserAccount(view.getContext(),
                        String.valueOf(loginInfoResponse.getUserId()), null);
                admin.save(view.getContext());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<LoginInfoResponse>() {
            @Override
            public void call(LoginInfoResponse loginInfoResponse) {
                setLoginResult(LoginActivity.CODE_RESULT_SUCCESS);
                view.showLoginSuccess();
                ((Activity) view.getContext()).finish();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                exceptionDispatcher.dispatchException(throwable);
            }
        });
        subscriptions.add(subscribe);
    }

    @Override
    public void onLoginError(LoginException exception) {
        String message = exception.getMessage();
        view.showLoginError(message);
    }
}
