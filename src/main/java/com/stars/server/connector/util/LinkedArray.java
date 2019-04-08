package com.stars.server.connector.util;

import java.util.Objects;

/**
 * Created by zws on 2015/9/30.
 */
public class LinkedArray<T> {

    private Node[] array;
    private Node current;
    private int currentIndex = 0;

    public LinkedArray(int capacity) {
        array = new Node[capacity];
    }

    @SuppressWarnings("unchecked")
    public T next() {
        if (current == null) {
            for (int i = currentIndex; i < currentIndex + array.length; i++) {
                int idx = i % array.length;
                if (array[idx] != null) {
                    current = array[idx];
                    currentIndex = idx;
                    break;
                }
            }
            if (current == null) {
                return null;
            }
        }
        T element = (T) current.element;
        current = current.next;
        currentIndex = current.index;
        return element;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        Node node = array[index];
        return node != null ? (T) node.element : null;
    }

    @SuppressWarnings("unchecked")
    public T add(int index, T element) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        Objects.requireNonNull(element);
        T oldElement = null;
        if (array[index] != null) {
            oldElement = (T) array[index].element;
        }
        array[index] = new Node(index, element);
        buildLink(); // 重建链表
        if (oldElement != null) {
            current = array[index];
        }
        return oldElement;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }

        if (array[index] != null) {
            // 下标更新
            if (array[index] == current) {
                if (current != current.next) {
                    current = current.next;
                } else {
                    current = null;
                }
            }

            T oldElement = (T) array[index].element; // 获取就值
            array[index] = null; // 赋值为空
            buildLink(); // 重建链表
            return oldElement;
        } else {
            return null;
        }
    }

    private void buildLink() {
        Node head = null, prev = null;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                if (head == null) {
                    head = prev = array[i];
                } else {
                    prev.next = array[i];
                    prev = prev.next;
                }
            }
        }
        if (head != null) {
            prev.next = head;
        }
    }

    @Override
    public String toString() {
        Node head = null, cursor = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                head = cursor = array[i];
                break;
            }
        }
        sb.append("{index=").append(this.currentIndex).append(", list={");
        sb.append("{");
        if (head != null) {
            while (true) {
                sb.append(cursor.toString()).append(",");
                if (cursor.next == head) {
                    break;
                } else {
                    cursor = cursor.next;
                }
            }
        }
        sb.append("}}");
        return sb.toString();
    }

    private static class Node<T> {
        private T element;
        private int index;
        private Node next;

        public Node(int index, T element) {
            this.index = index;
            this.element = element;
        }

        @Override
        public String toString() {
            return "(" + index + ", " + element + ")";
        }
    }

    public static void main(String[] args) {
        LinkedArray<Integer> array = new LinkedArray<>(32);
        System.out.println(array);
        System.out.println("---------01");
        array.add(1, 100);
        System.out.println(array);
        System.out.println("---------02");
        array.add(0, 0);
        array.add(5, 500);
        array.add(31, 3100);
        array.add(2, 200);
        array.add(25, 2500);
        array.add(15, 1500);
        System.out.println(array);
        System.out.println("---------03");
        try {
            array.add(-1, -100);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println("---------04");
        try {
            array.add(32, 3200);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println("---------05");
        System.out.println("cursor: " + array.next());
        array.remove(1);
        System.out.println("cursor: " + array.next());
        System.out.println(array);
        System.out.println("---------06");
        array.remove(31);
        array.remove(25);
        array.remove(15);
        array.remove(2);
        array.remove(0);
        System.out.println("cursor: " + array.next());
        System.out.println(array);
        array.remove(5);
        System.out.println("cursor: " + array.next());
        System.out.println(array);
    }
}
