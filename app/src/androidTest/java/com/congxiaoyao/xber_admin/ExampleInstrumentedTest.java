package com.congxiaoyao.xber_admin;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.congxiaoyao.location.model.GpsSampleRspOuterClass;
import com.congxiaoyao.location.model.GpsSampleRspOuterClass.GpsSampleRsp;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrl;
import com.congxiaoyao.xber_admin.monitoring.model.TraceCtrlFactory;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.congxiaoyao.xber_admin", appContext.getPackageName());
    }

    @Test
    public void foo() {
        TraceCtrlFactory factory = new TraceCtrlFactory(50, 50, 50);
        TraceCtrl infinite = null;
        for (int i = 0; i < 1000; i++) {
            if (Math.random() < 0.5) {
                infinite = factory.infinite().init(0, GpsSampleRsp.getDefaultInstance());
            } else if (infinite != null) {
                infinite.recycle();
            }
        }
    }
}
