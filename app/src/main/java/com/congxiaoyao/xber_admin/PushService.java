package com.congxiaoyao.xber_admin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.util.Log;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskAndDriver;
import com.congxiaoyao.stopmlib.LifecycleEvent;
import com.congxiaoyao.stopmlib.Stomp;
import com.congxiaoyao.stopmlib.client.StompClient;
import com.congxiaoyao.stopmlib.client.StompMessage;
import com.congxiaoyao.xber_admin.publishedtask.PublishedTaskActivity;

import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action1;

public class PushService extends Service implements Action1<Throwable> {

    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String EXTRA_STOP = "EXTRA_STOP";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private boolean shouldDestroy = false;

    private StompClient stompClient;
    private String token = "";
    private String url = "";
    private Long userId = -1L;

    @Override
    public void onCreate() {
        super.onCreate();
        shouldDestroy = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startPushService(Context context, String url,
                                        String token, Long userId) {
        Intent service = new Intent(context, PushService.class);
        service.putExtra(EXTRA_TOKEN, token);
        service.putExtra(EXTRA_URL, url);
        service.putExtra(EXTRA_USER_ID, userId);
        context.startService(service);
    }

    public static void stopPushService(Context context) {
        Intent intent = new Intent(context, PushService.class);
        intent.putExtra(EXTRA_STOP, "EXTRA_STOP");
        context.startService(intent);
    }

    private void stopMySelf() {
        shouldDestroy = true;
        stopService(new Intent(this, PushService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldDestroy = false;
        //关闭服务
        if (intent.getStringExtra(EXTRA_STOP) != null) {
            stopMySelf();
            return super.onStartCommand(intent, flags, startId);
        }

        Log.d(TAG.ME, "onStartCommand: ");
        token = intent.getStringExtra(EXTRA_TOKEN);
        url = intent.getStringExtra(EXTRA_URL);
        userId = intent.getLongExtra(EXTRA_USER_ID, -1);

        if (checkParam() && (stompClient == null || !stompClient.isConnected())) {
            Log.d(TAG.ME, "onStartCommand: 正在开启");
            connect();
        }else {
            Log.d(TAG.ME, "onStartCommand: 这是重复开启");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean checkParam() {
        return token != null && token.length() != 0 && url != null && url.length() != 0;
    }

    public void connect() {
        Map<String, String> header = new HashMap<>();
        header.put(NetWorkConfig.AUTH_KEY, token);
        stompClient = Stomp.over(WebSocket.class, url, header);
        stompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                if (lifecycleEvent.getType() == LifecycleEvent.Type.CLOSED
                        || lifecycleEvent.getType() == LifecycleEvent.Type.ERROR) {
                    Log.d(TAG.ME, "call: error~");
                    stopMySelf();
                }
            }
        }, this);
        stompClient.onConnected().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.d(TAG.ME, "call: 连接成功，正在订阅");
                topic();
            }
        }, this);
        stompClient.connect();
    }

    private Subscription topic() {
        return stompClient.topic(NetWorkConfig.TASK_STATUS_CHANGE)
                .subscribe(new Action1<StompMessage>() {
                    @Override
                    public void call(StompMessage message) {
                        handleStompMessage(message);
                    }
                }, this);
    }

    private void handleStompMessage(StompMessage message) {
        Log.d(TAG.ME, "handleStompMessage: ");
        if (message.isSubscribeCallback()) {
            Log.d(TAG.ME, "handleStompMessage: 订阅推送完成!");
            return;
        }
        String payload = message.getPayload();
        TaskAndDriver taskAndDriver = GsonHelper.getInstance()
                .fromJson(payload, TaskAndDriver.class);
        Log.d(TAG.ME, "handleStompMessage: task = " + taskAndDriver);
        if (taskAndDriver.getCreateUser().equals(Long.valueOf(1))
                || taskAndDriver.getCreateUser().equals(userId)) {
            showNotification(taskAndDriver);
        }
    }

    private void showNotification(TaskAndDriver task) {
        Intent intent = new Intent(this, PublishedTaskActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Icon icon = Icon.createWithResource(this, R.mipmap.ic_launcher);
        Notification notification = new Notification.Builder(this)
                .setLargeIcon(icon)
                .setSmallIcon(icon)
                .setContentTitle(task.getStatus() == Task.STATUS_EXECUTING ? "司机已出发" :
                        "任务完成")
                .setContentText(task.getStatus() == Task.STATUS_EXECUTING ?
                        String.format(Locale.CHINA, "司机%s正在执行任务%d...",
                                task.getCarDetail().getUserInfo().getName(), task.getTaskId()) :
                        "运输任务" + task.getTaskId() + "已经完成")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(this.NOTIFICATION_SERVICE);
        notificationManager.notify((int) ((long) task.getCarId()), notification);
    }

    @Override
    public void call(Throwable throwable) {
        Log.d(TAG.ME, "call: ", throwable);
        stopMySelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG.ME, "onDestroy: " + shouldDestroy);
        if (!shouldDestroy) {
            startPushService(this, url, token,userId);
        }else {
            if (stompClient != null) {
                stompClient.disconnect();
                stompClient = null;
            }
        }
    }
}
