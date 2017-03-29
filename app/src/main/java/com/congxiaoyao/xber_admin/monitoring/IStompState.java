package com.congxiaoyao.xber_admin.monitoring;

import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.xber_admin.service.SyncOrderedList;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public interface IStompState {

    void onCarAdd(long carId, SyncOrderedList<GpsSampleRspOuterClass.GpsSampleRsp> trace);

    void onCarRemove(long carId);
}
