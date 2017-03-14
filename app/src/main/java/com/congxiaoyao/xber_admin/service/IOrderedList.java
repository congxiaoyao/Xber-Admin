package com.congxiaoyao.xber_admin.service;

/**
 * Created by congxiaoyao on 2017/3/8.
 */

public interface IOrderedList<T> {

    /**
     * 将数组中的所有元素插入list中
     *
     * @param array
     */
    void insertAll(T[] array);

    /**
     * 将data插入到list中
     *
     * @param data
     */
    void insert(T data);

    /**
     * 返回但不移除队列中第一个元素 如果不存在则返回null
     *
     * @return
     */
    T getFirst();

    /**
     * 返回但不移除队列中最后一个元素 如果不存在则返回null
     *
     * @return
     */
    T getLast();

    /**
     * 返回并移除队列中第一个元素 如果不存在则返回null
     *
     * @return
     */
    T takeFirst();

    /**
     * 返回并移除队列中最后一个元素 如果不存在则返回null
     *
     * @return
     */
    T takeLast();

    /**
     * @return list的长度
     */
    int size();

    /**
     * 是否为空list
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 清除list中所有元素
     */
    void clear();
}
