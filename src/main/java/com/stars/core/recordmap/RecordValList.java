package com.stars.core.recordmap;

import com.stars.core.module.ModuleContext;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by zhaowenshuo on 2016/10/19.
 */
public class RecordValList<E> implements List<E> {

    private Class<E> elemClass;
    private List<String> innerList;
    private RoleRecord record;
    private ModuleContext context;

    private Constructor<E> elemConstructor;

    public RecordValList(Class<E> elemClass, List<String> innerList, RoleRecord record, ModuleContext context) {
        Objects.requireNonNull(elemClass);
        Objects.requireNonNull(innerList);
        if (!checkClass(elemClass)) {
            throw new IllegalArgumentException("Incorrect Class, elemClass=" + elemClass);
        }
        try {
            this.elemClass = elemClass;
            this.innerList = innerList;
            this.record = record;
            this.context = context;
            elemConstructor = elemClass.getConstructor(String.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void update() {
        context.update(record);
    }

    private boolean checkClass(Class clazz) {
        if (clazz == String.class
                || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class
                || clazz == Float.class || clazz == Double.class) {
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return innerList.size();
    }

    @Override
    public boolean isEmpty() {
        return innerList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return innerList.contains(o.toString());
    }

    @Override
    public Iterator<E> iterator() {
        return new InnerIterator();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        boolean ret = innerList.add(e.toString());
        update();
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = innerList.remove(o.toString());
        update();
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        innerList.clear();
        update();
    }

    @Override
    public E get(int index) {
        try {
            String e = innerList.get(index);
            return e != null ? elemConstructor.newInstance(e) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public E set(int index, E element) {
        try {
            String e = innerList.set(index, element.toString());
            update();
            return e != null ? elemConstructor.newInstance(e) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(int index, E element) {
        innerList.add(index, element.toString());
        update();
    }

    @Override
    public E remove(int index) {
        try {
            String e = innerList.remove(index);
            update();
            return e != null ? elemConstructor.newInstance(e) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int indexOf(Object o) {
        return innerList.indexOf(o.toString());
    }

    @Override
    public int lastIndexOf(Object o) {
        return innerList.lastIndexOf(o.toString());
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    class InnerIterator implements Iterator<E> {
        Iterator<String> it = innerList.iterator();
        @Override public boolean hasNext() { return it.hasNext(); }
        @Override
        public E next() {
            try {
                String e = it.next();
                return e != null ? elemConstructor.newInstance(e) : null;
            } catch (Exception e) {
                return null;
            }
        }
        @Override public void remove() { it.remove(); update(); }
    }
}
