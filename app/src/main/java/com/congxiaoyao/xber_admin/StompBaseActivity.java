package com.congxiaoyao.xber_admin;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.xber_admin.login.LoginActivity;
import com.congxiaoyao.xber_admin.service.StompService;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.utils.Token;
import com.congxiaoyao.xber_admin.widget.LoadingLayout;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/23.
 */

public abstract class StompBaseActivity extends AppCompatActivity implements StompService.OnCarChangeListener {

    private static final int MAX_RETRY = 3;

    protected long userId = -1;

    private ServiceConnection connection;
    protected StompService stompService;

    private Subscription shouldReconnect;
    private int connectCount = 0;

    private AlertDialog reconnectDialog;
    private int retryTimes = 0;

    private StompService.StompLifeCycle lifeCycle = new StompService.StompLifeCycle() {
        @Override
        public void onStompOpened() {
            shouldReconnect = Observable.just("连接超时\n请重新连接").delay(4, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            shouldReconnect(s);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d(TAG.ME, "call: ", throwable);
                        }
                    });
        }

        @Override
        public void onStompConnect() {
            Log.d(TAG.ME, "onStompConnect: ");
        }

        @Override
        public void onStompClose(int code) {
            Log.d(TAG.ME, "onStompClose: " + code);
            if (code == -1) {
                unbindService(connection);
                connection = null;
                if (isNetworkConnected()) {
                    login();
                }
            }
        }

        @Override
        public void onStompError() {
            Log.d(TAG.ME, "onStompError: ");
            shouldReconnect("Stomp服务发生错误 请重新连接");
        }

        @Override
        public void onNearestNPrepared() {
            connectCount++;
            if (connectCount == 2) {
                if (shouldReconnect != null) {
                    shouldReconnect.unsubscribe();
                }
                retryTimes = 0;
                onStompPrepared();
                getLoadingLayout().hideLoading();
            }
        }

        @Override
        public void onSpecifiedCarsPrepared() {
            onNearestNPrepared();
        }

        @Override
        public void onInnerError(Throwable throwable) {
            getLoadingLayout().hideLoading();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    finish();
                    startActivity(new Intent(StompBaseActivity.this, WelcomeActivity.class));
                }
            };
            new AlertDialog.Builder(StompBaseActivity.this)
                    .setTitle("内部错误")
                    .setMessage("发生了奇怪的错误 请重启软件")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            runnable.run();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            runnable.run();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            runnable.run();
                        }
                    }).show();
        }
    };

    private void login() {
        startActivity(new Intent(StompBaseActivity.this, LoginActivity.class));
    }

    @Override
    protected void onResume() {
        long pre = System.currentTimeMillis();
        super.onResume();
        Observable.just(1).map(new Func1<Integer, Admin>() {
            @Override
            public Admin call(Integer integer) {
                Admin admin = Admin.fromSharedPreference(StompBaseActivity.this);
                return admin;
            }
        }).filter(new Func1<Admin, Boolean>() {
            @Override
            public Boolean call(Admin admin) {
                return admin != null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Admin>() {
            @Override
            public void call(Admin admin) {
                Token.value = admin.getToken();
                StompBaseActivity.this.userId = admin.getUserId();
                tokenSafeOnResume(admin);
            }
        });
        Log.d("cxy", "on resume time = " + (System.currentTimeMillis() - pre));
    }

    protected void tokenSafeOnResume(Admin admin) {
        if (connection == null) {
            bindService();
        }
    }

    public void shouldReconnect(String message) {
        getLoadingLayout().hideLoading();
        //内部先进行三次重连
        if (retryTimes < MAX_RETRY) {
            retryTimes++;
            rebindService();
            return;
        }
        if (reconnectDialog != null) return;
        reconnectDialog = new AlertDialog.Builder(this).setTitle("错误")
                .setMessage(message).setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rebindService();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        reconnectDialog = null;
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        reconnectDialog = null;
                    }
                }).show();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public void rebindService() {
        unbindService(connection);
        Observable.just(0).delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        bindService();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG.ME, "rebindService" + throwable);
                        new AlertDialog.Builder(StompBaseActivity.this)
                                .setTitle("错误")
                                .setMessage("发生了奇怪的错误 请重启软件")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).show();
                    }
                });
    }

    public void bindService() {
        if (userId == -1 || Token.value.equals("")) {
            login();
            return;
        }
        reconnectDialog = null;
        connectCount = 0;
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                getLoadingLayout().showLoading();
                StompService.StompServiceBinder binder = (StompService.StompServiceBinder) service;
                stompService = binder.getStompService();
                stompService.setOnCarChangeListener(StompBaseActivity.this);
                long pre = System.currentTimeMillis();
                stompService.connect(lifeCycle);
                Log.d("cxy", "connect prepare time = " + (System.currentTimeMillis() - pre));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        StompService.bind(Token.value, userId, connection, this);
    }

    public void unBindService() {
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
    }

    @Override
    public void onCarAdd(long carId, SyncOrderedList<GpsSampleRspOuterClass.GpsSampleRsp> trace) {

    }

    @Override
    public void onCarRemove(long carId) {

    }


    protected void onStompPrepared() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) unbindService(connection);
        if (shouldReconnect != null) shouldReconnect.unsubscribe();
    }

    protected abstract LoadingLayout getLoadingLayout();

    public interface StompServiceProvider {
        StompService getService();
    }

}
