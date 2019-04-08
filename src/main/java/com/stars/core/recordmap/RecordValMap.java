package com.stars.core.recordmap;

import com.stars.core.module.ModuleContext;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by zhaowenshuo on 2016/10/18.
 */
public class RecordValMap<K, V> implements Map<K, V> {

    private Class<K> keyClass;
    private Class<V> valClass;
    private Map<String, String> innerMap;
    private RoleRecord record;
    private ModuleContext context;

    private Constructor<K> keyConstructor;
    private Constructor<V> valConstructor;

    public RecordValMap(Class<K> keyClass, Class<V> valClass, Map<String, String> innerMap, RoleRecord record, ModuleContext context) {
        Objects.requireNonNull(keyClass);
        Objects.requireNonNull(valClass);
        Objects.requireNonNull(innerMap);
        if (!checkClass(keyClass) || !checkClass(valClass)) {
            throw new IllegalArgumentException("Incorrect Class, keyClass=" + keyClass + ", valClass=" + valClass);
        }
        try {
            this.keyClass = keyClass;
            this.valClass = valClass;
            this.keyConstructor = keyClass.getConstructor(String.class);
            this.valConstructor = valClass.getConstructor(String.class);
            this.innerMap = innerMap;
            this.record = record;
            this.context = context;
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
    public String toString() {
        return innerMap.toString();
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            return innerMap.containsKey(key.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            return innerMap.containsValue(value.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public V get(Object key) {
        try {
            return valConstructor.newInstance(innerMap.get(key.toString()));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            V ret = valConstructor.newInstance(
                    innerMap.put(key.toString(), value.toString()));
            update();
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public V remove(Object key) {
        try {
            V ret = valConstructor.newInstance(innerMap.remove(key.toString()));
            update();
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        innerMap.clear();
        update();
    }

    @Override
    public Set<K> keySet() {
        return new InnerKeySet();
    }

    @Override
    public Collection<V> values() {
        return new InnerValues();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new InnerEntrySet();
    }

    class InnerKeySet extends AbstractSet<K> {
        Set<String> set = innerMap.keySet();
        @Override public Iterator<K> iterator() { return new InnerKeyIterator(set.iterator()); }
        @Override public int size() { return set.size(); }
        @Override public boolean contains(Object o) { try { return set.contains(o.toString()); } catch (Exception e) { return false; } }
        @Override public boolean remove(Object o) { try { boolean ret = set.remove(o.toString()); update(); return ret; } catch (Exception e) { return false; } }
        @Override public void clear() { set.clear(); update(); }
    }

    class InnerKeyIterator implements Iterator<K> {
        private Iterator<String> it;
        InnerKeyIterator(Iterator<String> it) { this.it = it; }
        @Override public boolean hasNext() { return it.hasNext(); }
        @Override public K next() { try { return keyConstructor.newInstance(it.next()); } catch (Exception e) { throw new IllegalStateException(); } }
        @Override public void remove() { it.remove(); update(); }
    }

    class InnerValues extends AbstractCollection<V> {
        Collection<String> collection = innerMap.values();
        @Override public Iterator<V> iterator() { return new InnerValueIterator(collection.iterator()); }
        @Override public int size() { return collection.size(); }
        @Override public boolean contains(Object o) { try { return collection.contains(o.toString()); } catch (Exception e) { return false; } }
        @Override public void clear() { collection.clear(); update(); }
    }

    class InnerValueIterator implements Iterator<V> {
        private Iterator<String> it;
        InnerValueIterator(Iterator<String> it) { this.it = it; }
        @Override public boolean hasNext() { return it.hasNext(); }
        @Override public V next() { try { return valConstructor.newInstance(it.next()); } catch (Exception e) { throw new IllegalStateException(); } }
        @Override public void remove() { it.remove(); update(); }
    }

    class InnerEntrySet extends AbstractSet<Map.Entry<K, V>> {
        Set<Map.Entry<String, String>> set = innerMap.entrySet();
        @Override public Iterator<Map.Entry<K, V>> iterator() { return new InnerEntryIterator(set.iterator()); }
        @Override public int size() { return set.size(); }
        @Override public boolean contains(Object o) {
            try {
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<K, V> fromEntry = (Entry<K, V>) o;
                AbstractMap.SimpleEntry toEntry = new AbstractMap.SimpleEntry(
                        fromEntry.getKey().toString(), fromEntry.getValue().toString());
                return set.contains(toEntry);
            } catch (Exception e) {
                return false;
            }
        }
        @Override public boolean remove(Object o) {
            try {
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<K, V> fromEntry = (Entry<K, V>) o;
                AbstractMap.SimpleEntry toEntry = new AbstractMap.SimpleEntry(
                        fromEntry.getKey().toString(), fromEntry.getValue().toString());
                boolean ret = set.remove(toEntry);
                if (ret) {
                    update();
                }
                return ret;
            } catch (Exception e) {
                return false;
            }
        }
        @Override public void clear() { set.clear(); update(); }
    }

    class InnerEntryIterator implements Iterator<Map.Entry<K, V>> {
        private Iterator<Map.Entry<String, String>> it;
        InnerEntryIterator(Iterator<Map.Entry<String, String>> it) { this.it = it; }
        @Override public boolean hasNext() { return it.hasNext(); }
        @Override public Map.Entry<K, V> next() {
            try {
                Map.Entry<String, String> fromEntry = (Entry<String, String>) it.next();
                AbstractMap.SimpleEntry toEntry = new AbstractMap.SimpleEntry(
                        keyConstructor.newInstance(fromEntry.getKey()), valConstructor.newInstance(fromEntry.getValue()));
                return toEntry;
            } catch (Exception e) {
                throw new IllegalStateException();
            }
        }
        @Override public void remove() { it.remove(); update(); }
    }
}
