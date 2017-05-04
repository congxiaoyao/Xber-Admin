package com.congxiaoyao.xber_admin;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.location.utils.Ray;
import com.congxiaoyao.xber_admin.monitoring.model.InfiniteTraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void foo() {
        List<Long> list = new ArrayList<>(50);
        for (int i = 0; i < list.size(); i++) {
            list.add((long) i);
        }
        XberRetrofit.create(CarRequest.class).getRunningCars(list)
                .subscribe(new Action1<List<Long>>() {
                    @Override
                    public void call(List<Long> longs) {
                        System.out.println(longs);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    public void print(int x) {
        if (x == -1) {
            System.out.println("超前");
        } else if (x == 1) {
            System.out.println("滞后");
        }
    }

}