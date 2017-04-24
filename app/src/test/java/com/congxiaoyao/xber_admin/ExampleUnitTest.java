package com.congxiaoyao.xber_admin;

import com.congxiaoyao.location.utils.Ray;
import com.congxiaoyao.xber_admin.monitoring.model.InfiniteTraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;
import com.congxiaoyao.xber_admin.utils.MathUtils;

import org.junit.Test;

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

    public void print(int x) {
        if (x == -1) {
            System.out.println("超前");
        } else if (x == 1) {
            System.out.println("滞后");
        }
    }

}