package com.congxiaoyao.xber_admin.driverslist.taskdetail;

import android.support.annotation.NonNull;

import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.httplib.response.GpsSamplePo;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;

import java.util.List;

/**
 * Created by congxiaoyao on 2017/4/1.
 */

public interface TraceContract {

    interface View extends LoadableView<Presenter> {

        void showTrace(List<LatLng> list);

        void showError();
    }

    interface Presenter extends BasePresenter {

        long getTaskId();

        @NonNull
        String getImageFileName();

        @NonNull
        String getLatLngFileName();

        @NonNull
        String getBoundsFileName();
    }
}
