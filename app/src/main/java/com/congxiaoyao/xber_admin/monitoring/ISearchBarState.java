package com.congxiaoyao.xber_admin.monitoring;

import java.util.List;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public interface ISearchBarState {

    void onTraceAllCar();

    void onTraceSpecifiedCar(List<Long> carIds);
}
