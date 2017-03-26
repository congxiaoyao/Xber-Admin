package com.congxiaoyao.xber_admin.monitoring.model;

import java.util.LinkedList;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class TraceCtrlFactory {

    private RecycleBin<NormalTraceCtrl> normalBin;
    private RecycleBin<BazierTraceCtrl> bazierBin;
    private RecycleBin<InfiniteTraceCtrl> infBin;

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

    public void clear() {
        clearRecycleBin(bazierBin);
        clearRecycleBin(normalBin);
        clearRecycleBin(infBin);
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
                return createTraceCtrl();
            }
            return linkedList.getLast();
        }

        public void recycle(T t) {
            if (linkedList.size() < maxSize) {
                linkedList.addLast(t);
            }
        }

        public T createTraceCtrl() {
            return null;
        }
    }
}

