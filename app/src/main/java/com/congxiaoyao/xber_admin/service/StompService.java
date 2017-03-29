package com.congxiaoyao.xber_admin.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.location.model.NearestNCarsQueryMessage;
import com.congxiaoyao.location.model.SpecifiedCarsQueryMessage;
import com.congxiaoyao.location.utils.GPSEncoding;
import com.congxiaoyao.location.utils.GpsUtils;
import com.congxiaoyao.stopmlib.LifecycleEvent;
import com.congxiaoyao.stopmlib.Stomp;
import com.congxiaoyao.stopmlib.client.StompClient;
import com.congxiaoyao.stopmlib.client.StompMessage;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.google.protobuf.InvalidProtocolBufferException;

import org.java_websocket.WebSocket;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;

public class StompService extends Service implements Action1<Throwable> {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";

    private Long userId;
    private String token;

    private Map<Long, SyncOrderedList<GpsSampleRsp>> buffer;
    private Map<Long, Long> latestTime;

    private StompLoopQuery nearestNQuery = new StompLoopQuery(
            NetWorkConfig.NEAREST_N_RSP_PATH,
            NetWorkConfig.NEAREST_N_ASK_PATH);

    private StompLoopQuery specifiedQuery = new StompLoopQuery(
            NetWorkConfig.SPECIFIED_RSP_PATH,
            NetWorkConfig.SPECIFIED_ASK_PATH
    );

    private StompClient client;

    private OnCarChangeListener onCarChangeListener;

    private Scheduler stompScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    private BehaviorSubject<Integer> serviceDestroy = BehaviorSubject.create();
    private StompLifeCycle lifeCycle;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void bind(String token, Long userId,
                            ServiceConnection connection,
                            Context context) {
        Intent intent = new Intent(context, StompService.class);
        intent.putExtra(StompService.EXTRA_TOKEN, token);
        intent.putExtra(StompService.EXTRA_USER_ID, userId);
        context.bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        userId = intent.getLongExtra(EXTRA_USER_ID, -1);
        token = intent.getStringExtra(EXTRA_TOKEN);
        buffer = new HashMap<>();
        latestTime = new TreeMap<>();
        latestTime = Collections.synchronizedMap(latestTime);
        return new StompServiceBinder();
    }

    public void connect(String url, final StompLifeCycle lifeCycle) {
        if (lifeCycle == null) {
            throw new NullPointerException("lifeCycle can't be null");
        }
        this.lifeCycle = lifeCycle;

        if (token == null || "".equals(token)) {
            lifeCycle.onStompError();
            return;
        }

        if (url == null || "".equals(url)) {
            lifeCycle.onStompError();
            return;
        }

        if (client != null && client.isConnected()) return;

        Action1<LifecycleEvent> lifecycleEvent = new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case CLOSED:
                        lifeCycle.onStompClose(lifecycleEvent.getCode());
                        break;
                    case ERROR:
                        lifeCycle.onStompError();
                        break;
                }
            }
        };
        Map<String, String> header = new HashMap<>();
        header.put(NetWorkConfig.AUTH_KEY, token);
        client = Stomp.over(WebSocket.class, url, header);
        client.lifecycle()
                .subscribeOn(stompScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG.ME, "lifecycle Event", throwable);
                    }
                });

        //连接完成的回调
        client.onConnected().compose(RxUtils.toMainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                nearestNQuery.topic().subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        lifeCycle.onNearestNPrepared();
                    }
                }, StompService.this);
                specifiedQuery.topic().subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        lifeCycle.onSpecifiedCarsPrepared();
                    }
                }, StompService.this);
                lifeCycle.onStompConnect();
            }
        });
        client.connect();
    }

    public void connect(StompLifeCycle lifeCycle) {
        connect(NetWorkConfig.WS_URL, lifeCycle);
    }

    public void nearestNTrace(final double lat, final double lng, final double r, final int n) {
        if (client == null) return;

        //准备好查询信息
        NearestNCarsQueryMessage queryMessage = new NearestNCarsQueryMessage();
        queryMessage.setLatitude(lat);
        queryMessage.setLongitude(lng);
        queryMessage.setNumber(n);
        queryMessage.setRadius(r);
        queryMessage.setUserId(userId);
        long queryId = (long) (Math.random() * 10000000);
        queryMessage.setQueryId(queryId);

        //如果 specifiedQuery没在运行 则不是状态切换 可以直接查询
        if (!specifiedQuery.isRunning()) {
            nearestNQuery.query(queryMessage, queryId, false);
            return;
        }

        //否则停掉 specifiedQuery 开始清理状态
        specifiedQuery.stopQuery();
        final Map<Long, ?> oldBuffer = buffer;
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer o) {
                buffer = new HashMap<>();
                latestTime.clear();
                latestTime = new TreeMap<>();
            }
        }).flatMap(new Func1<Integer, Observable<Long>>() {
            @Override
            public Observable<Long> call(Integer integer) {
                return Observable.from(oldBuffer.keySet());
            }
        }).filter(new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long aLong) {
                return onCarChangeListener != null;
            }
        }).subscribe(new Action1<Long>() {
            @Override
            public void call(Long id) {
                onCarChangeListener.onCarRemove(id);
            }
        }, StompService.this, new Action0() {
            @Override
            public void call() {
                oldBuffer.clear();
            }
        });
        nearestNQuery.query(queryMessage, queryId, false);
    }

    public void stopNearestNTrace() {
        nearestNQuery.stopQuery();
    }

    public void specifiedCarsTrace(final List<Long> carIds) {
        if (carIds == null || carIds.size() == 0
                || client == null) return;

        //准备好查询信息
        SpecifiedCarsQueryMessage queryMessage = new SpecifiedCarsQueryMessage();
        queryMessage.setCarIds(carIds);
        queryMessage.setUserId(userId);
        long queryId = (long) (Math.random() * 10000000);
        queryMessage.setQueryId(queryId);

        //如果 nearestNQuery没在运行 则不是状态切换 可以直接查询
        if (!nearestNQuery.isRunning()) {
            specifiedQuery.query(queryMessage, queryId, carIds.size() == 1);
            return;
        }
        //否则停掉 nearestNQuery 开始清理状态
        nearestNQuery.stopQuery();

        final Map<Long, SyncOrderedList<GpsSampleRsp>> newBuffer = new HashMap<>(carIds.size());
        final Map<Long, Long> newLatestDataTimeMap = new HashMap<>();

        Observable.from(carIds).observeOn(stompScheduler).subscribe(new Action1<Long>() {
            @Override
            public void call(Long carId) {
                //copying 算法 将有用的数据挪出来 然后整个清除掉
                SyncOrderedList<GpsSampleRsp> trace = buffer.get(carId);
                if (trace != null) {
                    newBuffer.put(carId, trace);
                    buffer.remove(carId);
                }
                Long time = latestTime.get(carId);
                if (time != null) {
                    newLatestDataTimeMap.put(carId, time);
                }
            }
        }, StompService.this, new Action0() {
            @Override
            public void call() {
                Set<Long> needToRemoveIds = buffer.keySet();
                final Map orgBuffer = buffer;
                final Map orgLatestTime = latestTime;
                //在stompScheduler线程应用准备好的newBuffer和newLatestTime
                logThreadName("重置buffer");
                buffer = newBuffer;
                latestTime = newLatestDataTimeMap;
                Observable.from(needToRemoveIds).compose(RxUtils.toMainThread()).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long carId) {
                        //在主线程做回调
                        if (onCarChangeListener != null) onCarChangeListener.onCarRemove(carId);
                    }
                }, StompService.this, new Action0() {
                    @Override
                    public void call() {
                        logThreadName("内存清理");
                        //在主线程清理内存
                        orgBuffer.clear();
                        orgLatestTime.clear();
                    }
                });
            }
        });

        specifiedQuery.query(queryMessage, queryId, carIds.size() == 1);
    }

    public void stopSpecifiedCarsTrace() {
        specifiedQuery.stopQuery();
    }

    public void disconnect() {
        client.disconnect();
    }

//    public void clear() {
//        client.clear();
//    }

    public void setOnCarChangeListener(OnCarChangeListener onCarChangeListener) {
        this.onCarChangeListener = onCarChangeListener;
    }

    /**
     * 处理错误
     *
     * @param throwable
     */
    @Override
    public void call(Throwable throwable) {
        Log.e(TAG.ME, "handle exception：", throwable);
        lifeCycle.onInnerError(throwable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG.ME, "Service onDestroy: ");

        serviceDestroy.onNext(0);

        nearestNQuery.close();
        nearestNQuery = null;

        specifiedQuery.close();
        specifiedQuery = null;

        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }

        if (latestTime != null) {
            latestTime.clear();
            latestTime = null;
        }

        disconnect();
    }

    private class StompLoopQuery {

        private final String topicPath;
        private final String queryPath;

        private boolean isVarFreq = false;

        private Subscription subscription;
        private LoopTask loopTask;
        private boolean isSubscribed = false;
        private ConnectableObservable<Integer> subscribeCallback;

        private long lastQueryId;

        StompLoopQuery(String topicPath, String requestPath) {
            this.topicPath = topicPath;
            this.queryPath = requestPath;
        }

        Observable<Integer> topic() {
            subscribeCallback = Observable.just(0)
                    .compose(RxUtils.toMainThread()).publish();
            if (client == null) return subscribeCallback;
            if (subscription != null) subscription.unsubscribe();

            subscription = client.topic(topicPath.replace("{userId}", String.valueOf(userId)))
                    .observeOn(stompScheduler)
                    .subscribe(new Action1<StompMessage>() {
                        @Override
                        public void call(StompMessage stompMessage) {
                            logThreadName();
                            handleStompMessage(stompMessage);
                        }
                    }, StompService.this);
            return subscribeCallback;
        }

        void query(Object queryMessage, long queryId, boolean isVarFreq) {
            this.isVarFreq = isVarFreq;
            if (client == null) return;
            final String jsonQueryMessage = GsonHelper.getInstance().toJson(queryMessage);
            stopQuery();
            this.lastQueryId = queryId;
            loopTask = new LoopTask() {
                @Override
                protected void onLoop() {
                    client.send(queryPath, jsonQueryMessage).subscribe();
                }
            };
            if (isSubscribed) loopTask.start(QueryConfig.QUERY_DELAY_NORMAL);
        }

        void stopQuery() {
            if (loopTask != null) {
                loopTask.stop();
            }
        }

        boolean isRunning() {
            if (loopTask == null) return false;
            return loopTask.isRunning();
        }

        void close() {
            stopQuery();
            loopTask = null;
            subscription = null;
            subscribeCallback = null;
        }

        /**
         * 当收到消息的回调
         * 这条message有可能是订阅完成的回调 所以需要区分类别 分类处理
         *
         * @param message
         */
        private void handleStompMessage(StompMessage message) {
            //是否为订阅回调
            if (message.isSubscribeCallback()) {
                isSubscribed = true;
                //通知外界订阅成功
                subscribeCallback.connect();
                //如果在连接完成之前就产生了请求 则发送之
                if (loopTask != null) loopTask.start(QueryConfig.QUERY_DELAY_NORMAL);
                return;
            }
            //可能已经停止了查询 即使回来了数据也不能再解析了
            if (loopTask == null || !loopTask.isRunning()) return;
            //处理真正的车辆轨迹信息
            byte[] payload = message.getBytePayload();
            GpsSampleRsp lastData = decodeAndBufferTrace(payload);
            //分析最后一个点 有必要的话加快查询速度(可能在过弯)
            if (lastData == null) {
                loopTask.setDelay(QueryConfig.QUERY_DELAY_NORMAL);
                return;
            }
            double angle = GpsUtils.getSpeedAngle(lastData.getLng(), lastData.getLat());
            angle = Math.abs(angle);
            if (angle > QueryConfig.TURNING_ANGLE && isVarFreq) {
                loopTask.setDelay(QueryConfig.QUERY_DELAY_URGENT);
            }else {
                loopTask.setDelay(QueryConfig.QUERY_DELAY_NORMAL);
            }
        }

        /**
         * 一般来说 参数payload为某一辆车的轨迹数据 此方法反序列化数据并做相应处理
         * <p>
         * 主要工作如下：
         * 1、解析数据
         * 2、存储数据 将数据加入缓存 如果数据不够新或者数据已经失效(queryId发生了变更) 则返回
         * 3、过期检查 启动定时器一段时间后检查是否此辆车的数据已过期 如果过期触发相应回调
         * 4、缓存检查 此辆车的数据是否为首次加入缓存，如果是则触发相应回调
         *
         * @param payload
         */
        private GpsSampleRsp decodeAndBufferTrace(byte[] payload) {
            //按分隔符分割byte数组
            byte[][] bytes = GPSEncoding.splitArray(payload);
            //没有数据直接返回
            if (bytes.length == 0) return null;
            GpsSampleRsp[] gpsSamples = new GpsSampleRsp[bytes.length];
            //分条解码gpsSample数据
            for (int i = 0; i < bytes.length; i++) {
                try {
                    gpsSamples[i] = GpsSampleRsp.parseFrom(GPSEncoding.decode(bytes[i]));
                    if (gpsSamples[i].getQueryId() != lastQueryId) return null;
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            //准备存储数据 等待消费
            GpsSampleRsp latest = gpsSamples[gpsSamples.length - 1];
            final long carId = latest.getCarId();

            int startIndex = 0;
            Long refTime = latestTime.get(carId);
            if (refTime != null) {
                int length = gpsSamples.length;
                while (startIndex < length &&
                        gpsSamples[startIndex].getTime() <= refTime) {
                    startIndex++;
                }
                //没能存储任何数据 这些数据全都请求过了
                if (startIndex == gpsSamples.length) {
                    return null;
                }
                //保留最新的数据
                gpsSamples = Arrays.copyOfRange(gpsSamples, startIndex, gpsSamples.length);
            }

            SyncOrderedList<GpsSampleRsp> trace = buffer.get(carId);
            final boolean newCar = (trace == null);
            if (newCar) {
                trace = new SyncOrderedList<>(new Comparator<GpsSampleRsp>() {
                    @Override
                    public int compare(GpsSampleRsp o1, GpsSampleRsp o2) {
                        return (int) (o1.getTime() - o2.getTime());
                    }
                });
                buffer.put(carId, trace);
            }

            //插入数据
            trace.insertAll(gpsSamples);

            for (int i = 0; i < gpsSamples.length; i++) {
                GpsSampleRsp gpsSample = gpsSamples[i];
                Log.d(TAG.ME, "receive data time = " + gpsSample.getTime()
                        + ",id = " + gpsSample.getCarId());
            }

            //一段时间后检查有没有新数据插入 没有的话认为这个车消失了 做相应处理并回调相应接口
            GpsSampleRsp last = trace.getLast();
            latestTime.put(last.getCarId(), last.getTime());
            Observable.just(last.getTime())
                    .delay(QueryConfig.DATA_EXPIRATION, QueryConfig.TIME_UNIT)
                    .takeUntil(serviceDestroy)
                    .subscribeOn(Schedulers.io())
                    .filter(new Func1<Long, Boolean>() {
                        @Override
                        public Boolean call(Long aLong) {
                            Long time = latestTime.get(carId);
                            boolean expiration = aLong.equals(time);
                            //车消失了 已经好久获取不到新数据了
                            if (expiration) {
                                SyncOrderedList<GpsSampleRsp> value = buffer.remove(carId);
                                expiration = expiration && (value != null);
                            }
                            //满足数据过期 确实删除了数据 监听器非空 则符合回调条件
                            return expiration && onCarChangeListener != null;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            onCarChangeListener.onCarRemove(carId);
                        }
                    }, StompService.this);

            //如果是新车的数据 则回调相应接口
            Observable.just(trace)
                    .filter(new Func1<SyncOrderedList<GpsSampleRsp>, Boolean>() {
                        @Override
                        public Boolean call(SyncOrderedList<GpsSampleRsp> gpsSamples) {
                            return (newCar && onCarChangeListener != null);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<SyncOrderedList<GpsSampleRsp>>() {
                        @Override
                        public void call(SyncOrderedList<GpsSampleRsp> gpsSamples) {
                            onCarChangeListener.onCarAdd(carId, gpsSamples);
                        }
                    }, StompService.this);
            return last;
        }
    }

    private void logThreadName() {
        Log.d(TAG.ME, "Thread id = " + Thread.currentThread().getName());
    }

    private void logThreadName(String s) {
        Log.d(TAG.ME, s + " Thread id = " + Thread.currentThread().getName());
    }

    public class StompServiceBinder extends Binder {

        public StompService getStompService() {
            return StompService.this;
        }
    }

    public interface StompLifeCycle {

        void onStompConnect();

        void onStompClose(int code);

        void onStompError();

        void onNearestNPrepared();

        void onSpecifiedCarsPrepared();

        void onInnerError(Throwable throwable);
    }

    /**
     * 在某次数据获取完成后，当某辆车的数据突然出现在内存中
     * 或是在某次清理的过程中 将某辆车清除会通过此监听器通知外界
     */
    public interface OnCarChangeListener {

        /**
         * 当数据获取完毕后发现出现了新车的回调
         *
         * @param carId
         */
        void onCarAdd(long carId, SyncOrderedList<GpsSampleRsp> trace);

        /**
         * 当某辆车被清理出内存后的回调
         *
         * @param carId
         */
        void onCarRemove(long carId);
    }
}