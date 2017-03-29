package com.congxiaoyao.xber_admin.service;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * 保持元素有序的List 元素的顺序由构造函数的Comparator参数决定
 * 对于{@link OrderedList#insertAll(T[])} 如果数组元素顺序与comparator顺序相同
 * 则最好时间复杂度为O(M+N) 好过{@link SortedSet}的O(M*logN) 这便是它存在的意义
 * <p>
 * Created by congxiaoyao on 2016/11/21.
 */
public class OrderedList<T> implements IOrderedList<T>, Iterable<T> {

    private Comparator<T> comparator;

    private Node<T> header;
    private Node<T> tail;
    private int size = 0;

    public OrderedList(Comparator<T> comparator) {
        this.comparator = comparator;
        header = new Node<>();
        tail = header;
    }

    /**
     * 将数组中的所有元素插入list中
     *
     * @param array
     */
    @Override
    public void insertAll(T[] array) {
        if (array == null || array.length == 0) return;
        int index = 0;
        Node<T> refNode = header.next;
        if (refNode == null) {
            addLast(new Node<>(array[0]));
            refNode = tail;
            index++;
        }
        while (index < array.length) {
            refNode = insert(refNode, array[index++]);
        }
    }

    /**
     * 将data插入到list中
     *
     * @param data
     */
    @Override
    public void insert(T data) {
        if (data == null) return;
        if (size == 0) addLast(new Node<T>(data));
        else insert(header.next, data);
    }

    @Override
    public T getFirst() {
        Node<T> first = header.next;
        return first == null ? null : first.data;
    }

    @Override
    public T getLast() {
        return tail.data;
    }

    /**
     * 返回并移除队列中第一个元素 如果不存在则返回null
     *
     * @return
     */
    @Override
    public T takeFirst() {
        T data = getFirst();
        if (data != null) remove(header.next);
        return data;
    }

    /**
     * 返回并移除队列中最后一个元素 如果不存在则返回null
     *
     * @return
     */
    @Override
    public T takeLast() {
        T data = getLast();
        if (size != 0) remove(tail);
        return data;
    }

    /**
     * 从refNode开始 向左或向右寻找合适的位置插入
     *
     * @param refNode
     * @param data
     * @return
     */
    private Node<T> insert(Node<T> refNode, T data) {
        Node<T> node;

        if (isEquals(refNode.data, data)) {
            node = refNode;
        }else if (isOrder(refNode.data, data)) {
            node = insertBehind(refNode, data);
        } else {
            node = insertAbove(refNode, data);
        }
        return node;
    }

    /**
     * 从refNode向前寻找 将将data插入到链表中应该出现的位置
     *
     * @param refNode
     * @param data
     * @return
     */
    private Node<T> insertAbove(Node<T> refNode, T data) {
        Node<T> node = new Node<>(data);
        while ((refNode = refNode.next) != null) {
            if (isEquals(refNode, node)) {
                return refNode;
            }
            if (isOrder(refNode, node)) {
                linkAbove(node, refNode);
                return node;
            }
        }
        addLast(node);
        return tail;
    }

    /**
     * 从refNode向后寻找 将data插入到链表中应该出现的位置
     *
     * @param refNode
     * @param data
     * @return
     */
    private Node<T> insertBehind(Node<T> refNode, T data) {
        Node<T> node = new Node<>(data);
        while ((refNode = refNode.last) != header) {
            if (isEquals(refNode, node)) {
                return refNode;
            }
            if (!isOrder(refNode, node)) {
                linkBehind(node, refNode);
                return node;
            }
        }
        return header.next;
    }

    private void remove(Node<T> node) {
        node.last.next = node.next;
        if (node == tail) {
            tail = node.last;
        } else node.next.last = node.last;
        size--;
    }

    private void addLast(Node<T> node) {
        tail.next = node;
        node.last = tail;
        tail = node;
        size++;
    }

    /**
     * 将node节点连接到behind节点后面
     *
     * @param node
     * @param behind
     */
    private void linkBehind(Node<T> node, Node<T> behind) {
        if (behind == tail) {
            addLast(node);
            return;
        }
        node.next = behind.next;
        node.last = behind;
        behind.next.last = node;
        behind.next = node;
        size++;
    }

    /**
     * 将node节点连接到above节点的前面
     *
     * @param node
     * @param above
     */
    private void linkAbove(Node<T> node, Node<T> above) {
        node.next = above;
        node.last = above.last;
        above.last.next = node;
        above.last = node;
        size++;
    }

    /**
     * 序列 t1 t2 是否为comparator规定的顺序
     *
     * @param t1
     * @param t2
     * @return
     */
    private boolean isOrder(T t1, T t2) {
        return comparator.compare(t1, t2) > 0;
    }

    private boolean isOrder(Node<T> node1, Node<T> node2) {
        return isOrder(node1.data, node2.data);
    }

    private boolean isEquals(T t1, T t2) {
        return comparator.compare(t1, t2) == 0;
    }

    private boolean isEquals(Node<T> node1, Node<T> node2) {
        return isEquals(node1.data, node2.data);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        header = new Node<>();
        tail = header;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator<T> {

        Node<T> pointer;

        MyIterator() {
            pointer = header;
        }

        @Override
        public boolean hasNext() {
            return pointer != tail;
        }

        @Override
        public T next() {
            Node<T> node = pointer.next;
            pointer = pointer.next;
            return node.data;
        }
    }

    static class Node<T> {
        Node<T> next;
        Node<T> last;
        T data;

        Node() {
        }

        Node(T data) {
            this.data = data;
        }
    }
}