package com.congxiaoyao.xber_admin;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.congxiaoyao.adapter.base.binding.demo.RVBindingTestActivity;
import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.xber_admin.service.QueryConfig;
import com.congxiaoyao.xber_admin.service.StompService;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;
import com.congxiaoyao.xber_admin.utils.MultiTouchListener;
import com.congxiaoyao.xber_admin.utils.RxUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class ServiceDemoActivity extends AppCompatActivity implements StompService.OnCarChangeListener {

    private ServiceConnection connection;
    private StompService stompService;

    private StompService.StompLifeCycle lifeCycle = new StompService.StompLifeCycle() {
        @Override
        public void onStompConnect() {
            Log.d(TAG.ME, "onStompConnect: ");
        }

        @Override
        public void onStompClose(int code) {
            Log.d(TAG.ME, "onStompClose: " + code);
            if (code == -1) {
                Toast.makeText(stompService, "token 过期", Toast.LENGTH_SHORT).show();
                unbindService(connection);
                connection = null;
            }
        }

        @Override
        public void onStompError() {
            Log.d(TAG.ME, "onStompError: ");
        }

        @Override
        public void onNearestNPrepared() {
            ActionBar actionBar = getSupportActionBar();
            CharSequence title = actionBar.getTitle();
            title = title + " NearestN 完成";
            actionBar.setTitle(title);
        }

        @Override
        public void onSpecifiedCarsPrepared() {
            ActionBar actionBar = getSupportActionBar();
            CharSequence title = actionBar.getTitle();
            title = title + " Specified 完成";
            actionBar.setTitle(title);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_demo);
        getSupportActionBar().setTitle("订阅");
        injectButton();
//        bindService();

        initActionBar();
    }

    private void initActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        try {
            Field field = supportActionBar.getClass().getDeclaredField("mContainerView");
            field.setAccessible(true);
            ViewGroup bar = (ViewGroup) field.get(supportActionBar);
            for (int i = 0; i < bar.getChildCount(); i++) {
                View child = bar.getChildAt(i);
                child.setClickable(true);
                child.setOnTouchListener(new MultiTouchListener(){
                    @Override
                    public void onMultiTouch() {
                        unbindService(connection);
                        Observable.just(1).delay(1000, QueryConfig.TIME_UNIT)
                                .compose(RxUtils.toMainThread()).subscribe(new Action1() {
                            @Override
                            public void call(Object o) {
                                bindService();
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectButton() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_main);
        Method[] methods = getClass().getMethods();
        for (final Method method : methods) {
            BTN annotation = method.getAnnotation(BTN.class);
            if (annotation != null) {
                Button button = new Button(this);
                button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                button.setText(annotation.value().equals("") ?
                        method.getName() : annotation.value());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        try {
                            if (parameterTypes.length == 0) {
                                method.invoke(ServiceDemoActivity.this);
                            } else if (parameterTypes.length == 1 &&
                                    parameterTypes[0] == View.class) {
                                method.invoke(ServiceDemoActivity.this, v);
                            }
                        } catch (Exception e) {
                            Log.e(TAG.ME, "onClick: invoke", e);
                        }
                    }
                });
                layout.addView(button);
            }
        }
    }

    public void bindService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                StompService.StompServiceBinder binder = (StompService.StompServiceBinder) service;
                stompService = binder.getStompService();
                stompService.setOnCarChangeListener(ServiceDemoActivity.this);
                stompService.connect(lifeCycle);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        StompService.bind(NetWorkConfig.TOKEN_TEST, 998L, connection, this);
    }

    @BTN
    public void nearestN() {
        stompService.nearestNTrace(0, 0, 1000, 100);
    }

    @BTN("stop NearestN")
    public void stopNearestN() {
        stompService.stopNearestNTrace();
    }

    @BTN
    public void specified() {
        List<Long> list = Arrays.asList(2L);
        stompService.specifiedCarsTrace(list);
    }

    @BTN("stop Specified")
    public void stopSpecified() {
        stompService.stopSpecifiedCarsTrace();
    }

    @BTN
    public void jump() {
        startActivity(new Intent(this, RVBindingTestActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) unbindService(connection);
    }

    @Override
    public void onCarAdd(long carId, SyncOrderedList<GpsSampleRspOuterClass.GpsSampleRsp> trace) {
        Log.d(TAG.ME, "onCarAdd: " + carId);

    }

    @Override
    public void onCarRemove(long carId) {
        Log.d(TAG.ME, "onCarRemove: " + carId);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BTN {
        String value() default "";
    }
}
