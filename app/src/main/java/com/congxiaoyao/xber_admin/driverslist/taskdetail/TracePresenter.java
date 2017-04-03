package com.congxiaoyao.xber_admin.driverslist.taskdetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.GpsSamplePo;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.driverslist.module.LatLngConverter;
import com.congxiaoyao.xber_admin.driverslist.module.LatLngMapperImpl;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/4/1.
 */

public class TracePresenter extends BasePresenterImpl<TraceContract.View> implements
        TraceContract.Presenter, Action1<Throwable> {

    private final long taskId;

    public TracePresenter(TraceContract.View view, long taskId) {
        super(view);
        this.taskId = taskId;
    }

    @Override
    public void subscribe() {
        Subscription subscribe = Observable.just(getLatLngFileName()).flatMap(new Func1<String, Observable<byte[]>>() {
            @Override
            public Observable<byte[]> call(String latLngFileName) {
                try {
                    //读取缓存
                    byte[] bytes = toByteArray(view.getContext()
                            .openFileInput(latLngFileName).getChannel());
                    Log.d(TAG.ME, "call: read file success");
                    return Observable.just(bytes);
                } catch (IOException e) {
                    //网络请求
                    return XberRetrofit.create(TaskRequest.class).getCarTraceBytes(taskId, Token.value).flatMap(new Func1<ResponseBody, Observable<byte[]>>() {
                        @Override
                        public Observable<byte[]> call(ResponseBody responseBody) {
                            byte[] bytes;
                            try {
                                bytes = responseBody.bytes();
                                cacheLatLng(bytes);
                            } catch (IOException e1) {
                                throw new RuntimeException(e1);
                            }
                            //网络请求成功
                            Log.d(TAG.ME, "call: network success");
                            return Observable.just(bytes);
                        }
                    });
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<byte[]>() {
            @Override
            public void call(byte[] bytes) {
                //展示数据
                List<LatLng> list = LatLngConverter
                        .init(new LatLngMapperImpl())
                        .fromByteArray(bytes);
                view.showTrace(list);
            }
        }, this);
        subscriptions.add(subscribe);
    }

    /**
     * 缓存数据
     * @param bytes
     */
    public void cacheLatLng(byte[] bytes) {
        FileOutputStream outputStream = null;
        try {
            outputStream = view.getContext()
                    .openFileOutput(getLatLngFileName(), Context.MODE_PRIVATE);
            outputStream.write(bytes);
            Log.d(TAG.ME, "call: save data success");
        } catch (IOException e) {
            Log.e(TAG.ME, "cacheLatLng: ", e);
        }finally {
            if (outputStream != null) try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG.ME, "cacheLatLng: ", e);
            }
        }
    }

    public static byte[] toByteArray(FileChannel fc) throws IOException {
        try {
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } finally {
            fc.close();
        }
    }

    @Override
    public long getTaskId() {
        return taskId;
    }

    @NonNull
    @Override
    public String getImageFileName() {
        return "trace_" + taskId + ".webp";
    }

    @NonNull
    @Override
    public String getLatLngFileName() {
        return "trace_" + taskId + ".latlng";
    }

    @NonNull
    @Override
    public String getBoundsFileName() {
        return "trace_" + taskId + ".bounds";
    }

    @Override
    public void call(Throwable throwable) {
        exceptionDispatcher.dispatchException(throwable);
    }


    @Override
    public void onDispatchException(Throwable throwable) {
        super.onDispatchException(throwable);
        view.showError();
    }
}
