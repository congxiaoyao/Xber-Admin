package com.congxiaoyao.xber_admin.service;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 同步版OrderedList
 * 用于解决简单的生产者消费者问题，但与传统的解决方式有所不同
 * 缓冲区没有容量的限制，当数据被取光的时候 再次获取会返回null
 * 在list中没有数据的情况下 新数据的插入会导致{@link DataReceiveListener#onReceiveDataWhenListIsEmpty()}
 * 回调的产生，可以认为 这是一种'单线程版的生产者消费者问题',用于消息的轮询模型
 *
 * Created by congxiaoyao on 2017/3/8.
 */

public class SyncOrderedList<T> implements IOrderedList<T>, Iterable<T> {

    private OrderedList<T> list;
    private ReentrantLock lock;
    private DataReceiveListener listener;

    public SyncOrderedList(Comparator<T> comparator) {
        list = new OrderedList<>(comparator);
        lock = new ReentrantLock();
    }

    @Override
    public void insertAll(T[] array) {
        lock.lock();
        try {
            boolean empty = list.isEmpty();
            list.insertAll(array);
            if (empty && listener != null) {
                listener.onReceiveDataWhenListIsEmpty();
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void insert(T data) {
        lock.lock();
        try {
            boolean empty = list.isEmpty();
            list.insert(data);
            if (empty && listener != null) {
                listener.onReceiveDataWhenListIsEmpty();
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public T getFirst() {
        lock.lock();
        try {
            T first = list.getFirst();
            return first;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public T getLast() {
        lock.lock();
        try {
            T last = list.getLast();
            return last;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public T takeFirst() {
        lock.lock();
        try {
            T last = list.takeFirst();
            return last;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public T takeLast() {
        lock.lock();
        try {
            T last = list.takeLast();
            return last;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return list.size();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            return list.isEmpty();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            list.clear();
        }finally {
            lock.unlock();
        }
    }

    public void setCallback(DataReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public interface DataReceiveListener{

        /**
         * 在list中没有数据的情况下 当收到一条数据的时候 会产生这个回调
         */
        void onReceiveDataWhenListIsEmpty();
    }
}
