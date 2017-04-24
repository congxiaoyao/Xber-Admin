package com.congxiaoyao.xber_admin.monitoring.model;

import android.util.Log;

import com.congxiaoyao.xber_admin.TAG;

import java.util.LinkedList;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class TraceCtrlFactory {

    private RecycleBin<NormalTraceCtrl> normalBin;
    private RecycleBin<BazierTraceCtrl> bazierBin;
    private RecycleBin<InfiniteTraceCtrl> infBin;
    private RecycleBin<HoldPositionCtrl> holdBin;

    public TraceCtrlFactory(int normalSize, int bazierSize, int infSize) {
        normalBin = new RecycleBin<NormalTraceCtrl>(normalSize) {
            @Override
            public NormalTraceCtrl createTraceCtrl() {
                return new NormalTraceCtrl();
            }
        };
        bazierBin = new RecycleBin<BazierTraceCtrl>(bazierSize) {
            @Override
            public BazierTraceCtrl createTraceCtrl() {
                return new BazierTraceCtrl();
            }
        };
        infBin = new RecycleBin<InfiniteTraceCtrl>(infSize) {
            @Override
            public InfiniteTraceCtrl createTraceCtrl() {
                return new InfiniteTraceCtrl();
            }
        };
        holdBin = new RecycleBin<HoldPositionCtrl>(infSize) {
            @Override
            public HoldPositionCtrl createTraceCtrl() {
                return new HoldPositionCtrl();
            }
        };
    }

    public NormalTraceCtrl normal() {
        NormalTraceCtrl obtain = normalBin.obtain();
        obtain.recycleBin = normalBin;
        return obtain;
    }

    public BazierTraceCtrl bazier() {
        BazierTraceCtrl obtain = bazierBin.obtain();
        obtain.recycleBin = bazierBin;
        return obtain;
    }

    public InfiniteTraceCtrl infinite() {
        InfiniteTraceCtrl obtain = infBin.obtain();
        obtain.recycleBin = infBin;
        return obtain;
    }

    public HoldPositionCtrl hold() {
        HoldPositionCtrl obtain = holdBin.obtain();
        obtain.recycleBin = holdBin;
        return obtain;
    }

    public void clear() {
        clearRecycleBin(bazierBin);
        clearRecycleBin(normalBin);
        clearRecycleBin(infBin);
        clearRecycleBin(holdBin);
    }

    private void clearRecycleBin(RecycleBin bin) {
        bin.linkedList.clear();
    }

    static class RecycleBin<T extends TraceCtrl> {

        LinkedList<T> linkedList = new LinkedList<>();
        private int maxSize;

        public RecycleBin(int maxSize) {

            this.maxSize = maxSize;
        }

        public T obtain() {
            if (linkedList.isEmpty()) {
                T traceCtrl = createTraceCtrl();
                traceCtrl.id = (int) (Math.random() * 1000);
                Log.d(TAG.ME, "TraceCtrlFactory: new traceCtrl"+traceCtrl.id);
                return traceCtrl;
            }
            T t = linkedList.pollFirst();
            Log.d(TAG.ME, "TraceCtrlFactory: from recycle bin "+t.id);
            return t;
        }

        public void recycle(T t) {
            Log.d(TAG.ME, "TraceCtrlFactory: recycle traceCtrl" + t.id);
            if (linkedList.size() < maxSize) {
                if (t.isUsing) {
                    t.isUsing = false;
                    linkedList.addLast(t);
                }
            }
        }

        public T createTraceCtrl() {
            return null;
        }
    }
}

